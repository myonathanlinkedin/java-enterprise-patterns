import java.util.List;

public final class Main {
    public static void main(String[] args) {
        runTests();
        System.out.println("All tests passed.");
    }

    private static void runTests() {
        Trie trie = new Trie();

        // Test 1: Basic insertion and retrieval
        trie.insert("apple", 5);
        trie.insert("app", 3);
        trie.insert("application", 2);
        List<String> suggestions1 = trie.getSuggestions("app", 3);
        assert suggestions1.size() == 3;
        assert suggestions1.get(0).equals("apple");
        assert suggestions1.get(1).equals("app");
        assert suggestions1.get(2).equals("application");

        // Test 2: Frequency tie and lexicographic order
        trie.insert("banana", 4);
        trie.insert("band", 4);
        trie.insert("bandana", 1);
        List<String> suggestions2 = trie.getSuggestions("ban", 2);
        assert suggestions2.size() == 2;
        assert suggestions2.get(0).equals("banana");
        assert suggestions2.get(1).equals("band");

        // Test 3: Empty prefix returns top words
        List<String> suggestions3 = trie.getSuggestions("", 5);
        assert suggestions3.size() == 5;
        assert suggestions3.get(0).equals("apple");
        assert suggestions3.get(1).equals("banana");
        assert suggestions3.get(2).equals("band");
        assert suggestions3.get(3).equals("app");
        assert suggestions3.get(4).equals("application");

        // Test 4: No suggestions for unknown prefix
        List<String> suggestions4 = trie.getSuggestions("xyz", 3);
        assert suggestions4.isEmpty();

        // Test 5: Increment frequency
        trie.insert("test", 1);
        trie.increment("test");
        trie.increment("test");
        List<String> suggestions5 = trie.getSuggestions("t", 1);
        assert suggestions5.size() == 1;
        assert suggestions5.get(0).equals("test");

        // Test 6: Overwrite frequency on duplicate insert
        trie.insert("dup", 2);
        trie.insert("dup", 5);
        List<String> suggestions6 = trie.getSuggestions("du", 1);
        assert suggestions6.size() == 1;
        assert suggestions6.get(0).equals("dup");

        // Test 7: Long word handling
        String longWord = "supercalifragilisticexpialidocious";
        trie.insert(longWord, 1);
        List<String> suggestions7 = trie.getSuggestions("super", 1);
        assert suggestions7.size() == 1;
        assert suggestions7.get(0).equals(longWord);

        // Test 8: k larger than available suggestions
        List<String> suggestions8 = trie.getSuggestions("app", 10);
        assert suggestions8.size() == 3;

        // Test 9: Edge case - single character prefix
        trie.insert("a", 10);
        List<String> suggestions9 = trie.getSuggestions("a", 2);
        assert suggestions9.size() == 2;
        assert suggestions9.get(0).equals("a");
        assert suggestions9.get(1).equals("app");

        // Test 10: Edge case - prefix equals a word
        List<String> suggestions10 = trie.getSuggestions("app", 1);
        assert suggestions10.size() == 1;
        assert suggestions10.get(0).equals("apple");

        // All assertions passed
    }
}
