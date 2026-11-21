import java.util.*;
public class Main {
    public static void main(String[] args) {
        testBasicPutGet();
        testEvictionPolicy();
        testMissingKeysDetection();
        testInvalidParameters();
        System.out.println("All TideCache tests passed.");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void testBasicPutGet() {
        TideCache cache = new TideCache(2, 3);
        cache.put(0, "k1", "v1");
        cache.put(0, "k2", "v2");
        cache.put(1, "kA", 42);

        assertTrue("v1".equals(cache.get(0, "k1")), "Basic get failed for layer 0, k1");
        assertTrue("v2".equals(cache.get(0, "k2")), "Basic get failed for layer 0, k2");
        assertTrue(Integer.valueOf(42).equals(cache.get(1, "kA")), "Basic get failed for layer 1, kA");
        assertTrue(cache.get(0, "nonexistent") == null, "Expected null for missing key");
    }

    private static void testEvictionPolicy() {
        TideCache cache = new TideCache(1, 2); // capacity 2
        cache.put(0, "a", "A");
        cache.put(0, "b", "B");
        // Access 'a' to make it recently used
        assertTrue("A".equals(cache.get(0, "a")), "Access before eviction failed");
        // Insert third entry, should evict least-recently used ('b')
        cache.put(0, "c", "C");
        assertTrue(cache.get(0, "b") == null, "LRU eviction failed (b should be evicted)");
        assertTrue("A".equals(cache.get(0, "a")), "LRU eviction incorrectly removed recent entry");
        assertTrue("C".equals(cache.get(0, "c")), "New entry not present after eviction");
        assertTrue(cache.layerSize(0) == 2, "Layer size mismatch after eviction");
    }

    private static void testMissingKeysDetection() {
        TideCache cache = new TideCache(3, 5);
        cache.put(0, "x1", "X1");
        cache.put(2, "z1", "Z1");
        java.util.Map<Integer, java.util.List<String>> request = new java.util.HashMap<>();
        request.put(0, java.util.Arrays.asList("x1", "x2"));
        request.put(1, java.util.Arrays.asList("y1"));
        request.put(2, java.util.Arrays.asList("z1", "z2"));

        java.util.Map<Integer, java.util.List<String>> missing = cache.getMissingKeys(request);
        assertTrue(missing.size() == 2, "Missing map size incorrect");
        assertTrue(missing.get(0).size() == 1 && missing.get(0).contains("x2"), "Missing detection failed for layer 0");
        assertTrue(missing.get(1).size() == 1 && missing.get(1).contains("y1"), "Missing detection failed for layer 1");
        assertTrue(missing.get(2).size() == 1 && missing.get(2).contains("z2"), "Missing detection failed for layer 2");
    }

    private static void testInvalidParameters() {
        boolean caught = false;
        try {
            new TideCache(0, 5);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Constructor should reject non‑positive layer count");

        caught = false;
        try {
            TideCache cache = new TideCache(2, 2);
            cache.put(2, "k", "v"); // invalid layer index
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assertTrue(caught, "put should reject out‑of‑range layer index");

        caught = false;
        try {
            TideCache cache = new TideCache(2, 2);
            cache.get(-1, "k");
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assertTrue(caught, "get should reject negative layer index");
    }
}
