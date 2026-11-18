package lsm;

public class Main {
    public static void main(String[] args) {
        testBasicPutGet();
        testFlush();
        testCompaction();
        testOverwrite();
        testDeletion();
        System.out.println("All LSM‑Tree tests passed.");
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    private static void testBasicPutGet() {
        LSMEngine engine = new LSMEngine(5);
        engine.put("a", "1");
        engine.put("b", "2");
        assertEquals("1", engine.get("a"), "Basic get a");
        assertEquals("2", engine.get("b"), "Basic get b");
        assertEquals(null, engine.get("c"), "Missing key");
    }

    private static void testFlush() {
        LSMEngine engine = new LSMEngine(2); // small memtable to force flush
        engine.put("x", "10");
        engine.put("y", "20"); // triggers flush
        assertEquals(1, engine.sstableCount(), "Flush creates one SSTable");
        assertEquals("10", engine.get("x"), "Get after flush x");
        assertEquals("20", engine.get("y"), "Get after flush y");
    }

    private static void testCompaction() {
        LSMEngine engine = new LSMEngine(2);
        engine.put("k1", "v1");
        engine.put("k2", "v2"); // flush #1
        engine.put("k1", "v1b");
        engine.put("k3", "v3"); // flush #2
        assertEquals(2, engine.sstableCount(), "Two SSTables before compaction");
        engine.compact();
        assertEquals(1, engine.sstableCount(), "One SSTable after compaction");
        assertEquals("v1b", engine.get("k1"), "Compacted value for k1");
        assertEquals("v2", engine.get("k2"), "Compacted value for k2");
        assertEquals("v3", engine.get("k3"), "Compacted value for k3");
    }

    private static void testOverwrite() {
        LSMEngine engine = new LSMEngine(3);
        engine.put("dup", "first");
        engine.put("dup", "second");
        engine.put("dup", "third"); // still in memtable
        assertEquals("third", engine.get("dup"), "Overwrite within memtable");
        engine.flush();
        assertEquals("third", engine.get("dup"), "Overwrite after flush");
        engine.put("dup", "fourth");
        assertEquals("fourth", engine.get("dup"), "Overwrite after new write");
    }

    private static void testDeletion() {
        LSMEngine engine = new LSMEngine(2);
        engine.put("del", "alive");
        engine.put("keep", "stay");
        engine.put("del", null); // tombstone, triggers flush due to size
        assertEquals(null, engine.get("del"), "Deleted key returns null");
        assertEquals("stay", engine.get("keep"), "Other key unaffected");
        engine.compact();
        assertEquals(null, engine.get("del"), "Deletion survives compaction");
    }
}
