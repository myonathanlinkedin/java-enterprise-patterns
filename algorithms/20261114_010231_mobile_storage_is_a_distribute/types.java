import java.util.Objects;

/**
 * Immutable key/value pair used by the storage system.
 *
 * @param <K> type of the key, must be non‑null
 * @param <V> type of the value, may be null
 */
public final class KeyValue<K, V> {
    private final K key;
    private final V value;

    public KeyValue(K key, V value) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValue<?, ?>)) return false;
        KeyValue<?, ?> that = (KeyValue<?, ?>) o;
        return key.equals(that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}

/**
 * Minimal local API that a storage node must implement.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface StorageNode<K, V> {
    /**
     * Store a key/value pair on this node.
     *
     * @param kv the pair to store; key must be non‑null
     */
    void put(KeyValue<K, V> kv);

    /**
     * Retrieve the value associated with a key from this node.
     *
     * @param key the key to look up; must be non‑null
     * @return the stored value, or {@code null} if the key is absent
     */
    V get(K key);
}

/**
 * Simple in‑memory implementation of {@link StorageNode}.
 *
 * @param <K> key type
 * @param <V> value type
 */
public final class InMemoryNode<K, V> implements StorageNode<K, V> {
    private final java.util.Map<K, V> store = new java.util.HashMap<>();

    @Override
    public void put(KeyValue<K, V> kv) {
        store.put(kv.getKey(), kv.getValue());
    }

    @Override
    public V get(K key) {
        return store.get(key);
    }

    /**
     * Expose the underlying map for testing purposes only.
     */
    java.util.Map<K, V> internalMap() {
        return store;
    }
}
