import java.util.*;

public class Main {
    public static void main(String[] args) {
        runAllTests();
        System.out.println("All tests passed.");
    }

    private static void runAllTests() {
        testBasicInsertionAndRetrieval();
        testFrequencyOrdering();
        testLexicographicTieBreak();
        testKGreaterThanMatches();
        testEmptyPrefix();
        testNonExistingPrefix();
        testUpdateFrequency();
        testInvalidInputs();
    }

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

    private static void testBasicInsertionAndRetrieval() {
        Trie trie = new Trie();
        trie.insert("apple", 5);
        trie.insert("app", 3);
        trie.insert("apricot", 2);
        List<String> res = trie.getTopK("ap", 10);
        assertEquals(3, res.size(), "Basic retrieval size");
        assertTrue(res.containsAll(Arrays.asList("apple", "app", "apricot")), "Basic retrieval content");
    }

    private static void testFrequencyOrdering() {
        Trie trie = new Trie();
        trie.insert("cat", 1);
        trie.insert("car", 10);
        trie.insert("cap", 5);
        List<String> res = trie.getTopK("ca", 3);
        List<String> expected = Arrays.asList("car", "cap", "cat");
        assertEquals(expected, res, "Frequency ordering");
    }

    private static void testLexicographicTieBreak() {
        Trie trie = new Trie();
        trie.insert("dog", 7);
        trie.insert("dot", 7);
        trie.insert("dove", 7);
        List<String> res = trie.getTopK("do", 3);
        List<String> expected = Arrays.asList("dog", "dot", "dove"); // alphabetical
        assertEquals(expected, res, "Lexicographic tie‑break");
    }

    private static void testKGreaterThanMatches() {
        Trie trie = new Trie();
        trie.insert("hi", 4);
        trie.insert("hill", 2);
        List<String> res = trie.getTopK("hi", 5);
        List<String> expected = Arrays.asList("hi", "hill");
        assertEquals(expected, res, "k larger than available");
    }

    private static void testEmptyPrefix() {
        Trie trie = new Trie();
        trie.insert("alpha", 3);
        trie.insert("beta", 5);
        trie.insert("gamma", 1);
        List<String> res = trie.getTopK("", 2);
        List<String> expected = Arrays.asList("beta", "alpha");
        assertEquals(expected, res, "Empty prefix returns global top‑k");
    }

    private static void testNonExistingPrefix() {
        Trie trie = new Trie();
        trie.insert("sun", 2);
        List<String> res = trie.getTopK("moon", 3);
        assertEquals(0, res.size(), "Non‑existing prefix yields empty list");
    }

    private static void testUpdateFrequency() {
        Trie trie = new Trie();
        trie.insert("tree", 1);
        trie.insert("tree", 10); // update
        List<String> res = trie.getTopK("tr", 1);
        assertEquals(Collections.singletonList("tree"), res, "Frequency update reflected");
    }

    private static void testInvalidInputs() {
        Trie trie = new Trie();
        boolean caught = false;
        try {
            trie.insert(null, 1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Null word throws");

        caught = false;
        try {
            trie.insert("Invalid1", 1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Non‑lowercase word throws");

        caught = false;
        try {
            trie.getTopK("Bad$", 2);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Invalid prefix throws");
    }
}
