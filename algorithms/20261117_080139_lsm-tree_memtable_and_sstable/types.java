import java.util.Objects;

/**
 * Immutable key/value pair used throughout the LSM engine.
 */
public final class KVPair implements Comparable<KVPair> {
    private final String key;
    private final String value;

    public KVPair(String key, String value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(KVPair o) {
        return this.key.compareTo(o.key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KVPair)) return false;
        KVPair kv = (KVPair) o;
        return key.equals(kv.key) && value.equals(kv.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return key + "\t" + value;
    }
}
