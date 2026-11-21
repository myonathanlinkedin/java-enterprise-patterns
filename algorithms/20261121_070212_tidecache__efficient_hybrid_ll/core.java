import java.util.*;

public class TideCache {
    private final int numLayers;
    private final int capacityPerLayer;
    private final List<LRUCache> layerCaches;

    // LRU cache implementation using LinkedHashMap
    private static class LRUCache extends LinkedHashMap<String, Object> {
        private final int capacity;

        LRUCache(int capacity) {
            super(capacity, 0.75f, true); // access-order
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
            return size() > capacity;
        }
    }

    /**
     * Constructs a TideCache.
     *
     * @param numLayers          number of distinct model layers to cache
     * @param capacityPerLayer   maximum entries each layer can hold
     * @throws IllegalArgumentException if arguments are non‑positive
     */
    public TideCache(int numLayers, int capacityPerLayer) {
        if (numLayers <= 0 || capacityPerLayer <= 0) {
            throw new IllegalArgumentException("numLayers and capacityPerLayer must be positive");
        }
        this.numLayers = numLayers;
        this.capacityPerLayer = capacityPerLayer;
        this.layerCaches = new ArrayList<>(numLayers);
        for (int i = 0; i < numLayers; i++) {
            layerCaches.add(new LRUCache(capacityPerLayer));
        }
    }

    /**
     * Retrieves a cached value for a given layer and key.
     *
     * @param layer zero‑based layer index
     * @param key   unique identifier for the request (e.g., token sequence hash)
     * @return cached object or {@code null} if absent
     * @throws IndexOutOfBoundsException if layer index is invalid
     */
    public synchronized Object get(int layer, String key) {
        validateLayer(layer);
        return layerCaches.get(layer).get(key);
    }

    /**
     * Stores a value in the cache for the specified layer and key.
     *
     * @param layer zero‑based layer index
     * @param key   unique identifier
     * @param value value to cache (must be non‑null)
     * @throws IndexOutOfBoundsException if layer index is invalid
     * @throws NullPointerException      if key or value is null
     */
    public synchronized void put(int layer, String key, Object value) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        validateLayer(layer);
        layerCaches.get(layer).put(key, value);
    }

    /**
     * Given a map of layer → list of keys, returns a map of layer → list of keys
     * that are *not* present in the cache. Useful for determining which
     * sub‑computations must be executed.
     *
     * @param requests map where each entry contains a layer and the keys to query
     * @return map of missing keys per layer (empty lists omitted)
     * @throws IndexOutOfBoundsException if any layer index is invalid
     */
    public synchronized Map<Integer, List<String>> getMissingKeys(Map<Integer, List<String>> requests) {
        Map<Integer, List<String>> missing = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : requests.entrySet()) {
            int layer = entry.getKey();
            validateLayer(layer);
            List<String> keys = entry.getValue();
            if (keys == null) continue;
            List<String> miss = new ArrayList<>();
            for (String key : keys) {
                if (!layerCaches.get(layer).containsKey(key)) {
                    miss.add(key);
                }
            }
            if (!miss.isEmpty()) {
                missing.put(layer, miss);
            }
        }
        return missing;
    }

    /**
     * Clears all entries from every layer.
     */
    public synchronized void clear() {
        for (LRUCache cache : layerCaches) {
            cache.clear();
        }
    }

    /**
     * Returns the current size of a specific layer cache.
     *
     * @param layer zero‑based layer index
     * @return number of entries stored for that layer
     */
    public synchronized int layerSize(int layer) {
        validateLayer(layer);
        return layerCaches.get(layer).size();
    }

    private void validateLayer(int layer) {
        if (layer < 0 || layer >= numLayers) {
            throw new IndexOutOfBoundsException("Layer index out of range: " + layer);
        }
    }
}
