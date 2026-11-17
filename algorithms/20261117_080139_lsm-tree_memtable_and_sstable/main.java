import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Main {
    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg);
        }
    }

    public static void main(String[] args) throws Exception {
        // Create a temporary directory for the LSM storage.
        Path tempDir = Files.createTempDirectory("lsm-test-");
        try {
            // Parameters: flush after 5 entries, compact after 3 SSTables.
            LSMEngine engine = new LSMEngine(tempDir, 5, 3);

            // ---- Basic put/get before any flush ----
            engine.put("apple", "red");
            engine.put("banana", "yellow");
            assertEquals("red", engine.get("apple"), "Basic get after put");
            assertEquals("yellow", engine.get("banana"), "Basic get after put");
            assertEquals(null, engine.get("cherry"), "Missing key should be null");
            assertEquals(2, engine.memTableSize(), "MemTable size before flush");

            // ---- Trigger flush by exceeding threshold ----
            engine.put("cherry", "red");
            engine.put("date", "brown");
            engine.put("elderberry", "black"); // 5th entry -> flush
            assertEquals(0, engine.memTableSize(), "MemTable should be empty after flush");
            assertEquals(1, engine.sstableCount(), "One SSTable after first flush");
            assertEquals("black", engine.get("elderberry"), "Value after flush");

            // ---- Add more entries to create multiple SSTables ----
            engine.put("fig", "purple");
            engine.put("grape", "green");
            engine.put("honeydew", "green");
            engine.put("kiwi", "brown");
            engine.put("lemon", "yellow"); // flush #2
            assertEquals(2, engine.sstableCount(), "Two SSTables after second flush");

            engine.put("mango", "orange");
            engine.put("nectarine", "orange");
            engine.put("orange", "orange");
            engine.put("papaya", "orange");
            engine.put("quince", "yellow"); // flush #3 triggers compaction
            assertEquals(1, engine.sstableCount(), "Compaction should reduce to one SSTable");
            assertEquals(0, engine.memTableSize(), "MemTable empty after compaction");

            // ---- Verify that newer values win over older duplicates ----
            engine.put("apple", "green"); // new value for existing key
            engine.put("banana", "green");
            engine.put("cherry", "green");
            engine.put("date", "green");
            engine.put("elderberry", "green"); // flush triggers new SSTable
            assertEquals("green", engine.get("apple"), "Updated value after overwrite");
            assertEquals("green", engine.get("banana"), "Updated value after overwrite");
            assertEquals("green", engine.get("cherry"), "Updated value after overwrite");
            assertEquals("green", engine.get("date"), "Updated value after overwrite");
            assertEquals("green", engine.get("elderberry"), "Updated value after overwrite");

            // ---- Edge case: large number of keys ----
            for (int i = 0; i < 100; i++) {
                engine.put("key" + i, "val" + i);
            }
            // Force a flush
            engine.put("trigger", "flush");
            // Verify random sampling
            assertEquals("val42", engine.get("key42"), "Random key after many inserts");
            assertEquals("val99", engine.get("key99"), "Random key after many inserts");
            assertEquals("flush", engine.get("trigger"), "Trigger key present");

            // ---- Ensure that get on non‑existent key still returns null ----
            assertEquals(null, engine.get("nonexistent"), "Non‑existent key returns null");

            System.out.println("All LSM‑Tree tests passed.");
        } finally {
            // Clean up temporary files
            deleteRecursively(tempDir);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        // Ignored in cleanup
                    }
                });
    }
}
