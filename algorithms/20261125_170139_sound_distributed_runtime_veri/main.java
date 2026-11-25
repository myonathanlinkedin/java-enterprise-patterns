import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple test harness for EvidenceAggregator.
 *
 * The suite validates soundness under all three possible outcomes.
 */
public class Main {
    public static void main(String[] args) {
        testAllRequiredPresent();
        testMissingPredicate();
        testContradictoryEvidence();
        testDuplicateEvents();
        testOutOfOrderEvents();
        System.out.println("All tests passed.");
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void testAllRequiredPresent() {
        Set<String> required = new HashSet<>(Arrays.asList("login", "access_granted"));
        EvidenceAggregator agg = new EvidenceAggregator(required);

        agg.addEvent(new Event("nodeA", "login", 1));
        agg.addEvent(new Event("nodeB", "access_granted", 2));

        VerificationResult res = agg.verify();
        assertEquals(VerificationResult.TRUE, res, "testAllRequiredPresent");
    }

    private static void testMissingPredicate() {
        Set<String> required = new HashSet<>(Arrays.asList("login", "access_granted"));
        EvidenceAggregator agg = new EvidenceAggregator(required);

        agg.addEvent(new Event("nodeA", "login", 5));

        VerificationResult res = agg.verify();
        assertEquals(VerificationResult.UNKNOWN, res, "testMissingPredicate");
    }

    private static void testContradictoryEvidence() {
        Set<String> required = new HashSet<>(Arrays.asList("login", "access_granted"));
        EvidenceAggregator agg = new EvidenceAggregator(required);

        agg.addEvent(new Event("nodeA", "login", 3));
        agg.addEvent(new Event("nodeC", "NOT_access_granted", 4));

        VerificationResult res = agg.verify();
        assertEquals(VerificationResult.FALSE, res, "testContradictoryEvidence");
    }

    private static void testDuplicateEvents() {
        Set<String> required = new HashSet<>(Arrays.asList("heartbeat"));
        EvidenceAggregator agg = new EvidenceAggregator(required);

        Event e = new Event("nodeX", "heartbeat", 10);
        agg.addEvent(e);
        agg.addEvent(e); // duplicate should be ignored

        VerificationResult res = agg.verify();
        assertEquals(VerificationResult.TRUE, res, "testDuplicateEvents");
    }

    private static void testOutOfOrderEvents() {
        Set<String> required = new HashSet<>(Arrays.asList("init", "ready"));
        EvidenceAggregator agg = new EvidenceAggregator(required);

        // Events arrive out of logical timestamp order
        agg.addEvent(new Event("nodeY", "ready", 20));
        agg.addEvent(new Event("nodeY", "init", 15));

        VerificationResult res = agg.verify();
        assertEquals(VerificationResult.TRUE, res, "testOutOfOrderEvents");
    }
}
