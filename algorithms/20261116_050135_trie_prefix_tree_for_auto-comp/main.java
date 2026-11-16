import java.util.*;

public class Main {
    public static void main(String[] args) {
        testBasicInsertionAndLookup();
        testFrequencyRanking();
        testTieBreakingLexicographically();
        testEmptyPrefixReturnsGlobalTopK();
        testNonExistingPrefix();
        System.out.println("All tests passed.");
    }

    private static void testBasicInsertionAndLookup() {
        Trie trie = new Trie();
        trie.insert("apple");
        trie.insert("app");
        trie.insert("application");
        List<String> res = trie.getTopK("app", 10);
        assert res.size() == 3 : "Expected 3 results";
        assert res.containsAll(Arrays.asList("app", "apple", "application"));
    }

    private static void testFrequencyRanking() {
        Trie trie = new Trie();
        trie.insert("dog", 5);
        trie.insert("deer", 2);
        trie.insert("deal", 3);
        List<String> top2 = trie.getTopK("de", 2);
        assert top2.size() == 2 : "Expected 2 results";
        assert top2.get(0).equals("deal") : "deal should be first (freq 3)";
        assert top2.get(1).equals("deer") : "deer should be second (freq 2)";
    }

    private static void testTieBreakingLexicographically() {
        Trie trie = new Trie();
        trie.insert("cat", 4);
        trie.insert("car", 4);
        trie.insert("can", 4);
        List<String> top3 = trie.getTopK("ca", 3);
        assert top3.equals(Arrays.asList("can", "car", "cat")) :
               "Lexicographic tie‑break failed: " + top3;
    }

    private static void testEmptyPrefixReturnsGlobalTopK() {
        Trie trie = new Trie();
        trie.insert("zebra", 1);
        trie.insert("yak", 2);
        trie.insert("xenon", 3);
        List<String> top2 = trie.getTopK("", 2);
        assert top2.equals(Arrays.asList("xenon", "yak")) :
               "Global top‑k incorrect: " + top2;
    }

    private static void testNonExistingPrefix() {
        Trie trie = new Trie();
        trie.insert("alpha");
        trie.insert("beta");
        List<String> empty = trie.getTopK("gamma", 5);
        assert empty.isEmpty() : "Expected empty list for unknown prefix";
    }
}
