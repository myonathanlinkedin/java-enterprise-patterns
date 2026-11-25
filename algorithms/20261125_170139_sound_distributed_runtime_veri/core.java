import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Result of a verification query.
 */
enum VerificationResult {
    TRUE, FALSE, UNKNOWN
}

/**
 * Immutable representation of an event observed at runtime.
 */
final class Event {
    private final String source;   // identifier of the node that generated the event
    private final String type;     // logical predicate name (e.g., "login", "NOT_error")
    private final int timestamp;   // monotonically increasing logical time

    Event(String source, String type, int timestamp) {
        this.source = Objects.requireNonNull(source);
        this.type = Objects.requireNonNull(type);
        this.timestamp = timestamp;
    }

    String getSource() {
        return source;
    }

    String getType() {
        return type;
    }

    int getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event e = (Event) o;
        return timestamp == e.timestamp &&
               source.equals(e.source) &&
               type.equals(e.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, type, timestamp);
    }

    @Override
    public String toString() {
        return "Event{" + "source='" + source + '\'' + ", type='" + type + '\'' + ", timestamp=" + timestamp + '}';
    }
}

/**
 * Core component that aggregates evidence from distributed nodes and answers
 * verification queries under incomplete evidence.
 *
 * The algorithm is deliberately simple yet sound:
 *   - The property to verify is expressed as a set of required positive predicates.
 *   - Any observed predicate of the form "NOT_<p>" is a definitive contradiction for <p>.
 *   - TRUE is reported only when *all* required predicates have been observed.
 *   - FALSE is reported as soon as a contradiction for any required predicate is observed.
 *   - Otherwise the result is UNKNOWN.
 *
 * This guarantees that the system never reports TRUE when the property might be violated,
 * nor FALSE when the property might still hold.
 */
final class EvidenceAggregator {
    private final Set<String> requiredPredicates; // e.g., {"login", "access_granted"}
    private final Set<String> observedPositives = new HashSet<>();
    private final Set<String> observedNegatives = new HashSet<>();
    private final Set<Event> processedEvents = new HashSet<>();

    /**
     * Constructs an aggregator for a given property.
     *
     * @param requiredPredicates the set of predicate names that must all be true
     */
    EvidenceAggregator(Set<String> requiredPredicates) {
        if (requiredPredicates == null || requiredPredicates.isEmpty()) {
            throw new IllegalArgumentException("Required predicate set must be non‑empty");
        }
        this.requiredPredicates = Collections.unmodifiableSet(new HashSet<>(requiredPredicates));
    }

    /**
     * Incorporates a new event into the evidence base.
     * Duplicate events are ignored.
     *
     * @param e the event to add
     */
    void addEvent(Event e) {
        Objects.requireNonNull(e);
        // Idempotent handling of duplicates
        if (!processedEvents.add(e)) {
            return;
        }

        String type = e.getType();
        if (type.startsWith("NOT_")) {
            String negated = type.substring(4);
            observedNegatives.add(negated);
        } else {
            observedPositives.add(type);
        }
    }

    /**
     * Returns the current verification result based on accumulated evidence.
     *
     * @return TRUE, FALSE, or UNKNOWN
     */
    VerificationResult verify() {
        // Check for any contradiction first – this yields FALSE immediately.
        for (String req : requiredPredicates) {
            if (observedNegatives.contains(req)) {
                return VerificationResult.FALSE;
            }
        }

        // All required predicates observed positively?
        if (observedPositives.containsAll(requiredPredicates)) {
            return VerificationResult.TRUE;
        }

        // No contradiction, but not enough evidence.
        return VerificationResult.UNKNOWN;
    }

    /**
     * Exposes the set of required predicates (read‑only).
     *
     * @return unmodifiable view of required predicates
     */
    Set<String> getRequiredPredicates() {
        return requiredPredicates;
    }

    /**
     * Exposes the set of positively observed predicates (read‑only snapshot).
     *
     * @return copy of observed positive predicates
     */
    Set<String> getObservedPositives() {
        return new HashSet<>(observedPositives);
    }

    /**
     * Exposes the set of negatively observed predicates (read‑only snapshot).
     *
     * @return copy of observed negative predicates
     */
    Set<String> getObservedNegatives() {
        return new HashSet<>(observedNegatives);
    }
}
