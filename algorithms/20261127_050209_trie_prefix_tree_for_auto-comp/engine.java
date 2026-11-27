import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String word, int frequency) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word must be non-empty");
        }
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            node = node.children.computeIfAbsent(ch, k -> new TrieNode());
        }
        node.isWord = true;
        node.frequency = frequency;
    }

    public void increment(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word must be non-empty");
        }
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            node = node.children.computeIfAbsent(ch, k -> new TrieNode());
        }
        node.isWord = true;
        node.frequency += 1;
    }

    public List<String> getSuggestions(String prefix, int k) {
        if (prefix == null) {
            throw new IllegalArgumentException("Prefix must not be null");
        }
        if (k <= 0) {
            return Collections.emptyList();
        }
        TrieNode node = root;
        for (char ch : prefix.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) {
                return Collections.emptyList();
            }
        }
        List<WordFrequency> collected = new ArrayList<>();
        collectWords(node, new StringBuilder(prefix), collected);
        Collections.sort(collected);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(k, collected.size()); i++) {
            result.add(collected.get(i).word);
        }
        return result;
    }

    private void collectWords(TrieNode node, StringBuilder current, List<WordFrequency> list) {
        if (node.isWord) {
            list.add(new WordFrequency(current.toString(), node.frequency));
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            current.append(entry.getKey());
            collectWords(entry.getValue(), current, list);
            current.deleteCharAt(current.length() - 1);
        }
    }

    private static final class WordFrequency implements Comparable<WordFrequency> {
        final String word;
        final int frequency;

        WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(WordFrequency other) {
            if (this.frequency != other.frequency) {
                return Integer.compare(other.frequency, this.frequency); // descending
            }
            return this.word.compareTo(other.word); // ascending lexicographic
        }
    }
}
