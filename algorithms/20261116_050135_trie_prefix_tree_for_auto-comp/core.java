import java.util.*;

public class Trie {
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        int frequency = 0;          // frequency of the word ending here
        boolean isWord = false;    // true if a word ends at this node
    }

    private final Node root = new Node();

    /**
     * Inserts a word with default frequency 1. If the word already exists,
     * its frequency is increased by 1.
     *
     * @param word non‑null word consisting of Unicode characters
     */
    public void insert(String word) {
        insert(word, 1);
    }

    /**
     * Inserts a word with the given frequency. If the word already exists,
     * its frequency is increased by {@code freq}.
     *
     * @param word non‑null word
     * @param freq positive frequency to add
     * @throws IllegalArgumentException if word is null or freq ≤ 0
     */
    public void insert(String word, int freq) {
        if (word == null) throw new IllegalArgumentException("word cannot be null");
        if (freq <= 0) throw new IllegalArgumentException("frequency must be positive");
        Node cur = root;
        for (char ch : word.toCharArray()) {
            cur = cur.children.computeIfAbsent(ch, c -> new Node());
        }
        cur.isWord = true;
        cur.frequency += freq;
    }

    /**
     * Returns up to {@code k} words that start with {@code prefix},
     * ordered by descending frequency. Ties are resolved by lexicographic order.
     *
     * @param prefix prefix to search; may be empty but not null
     * @param k maximum number of results (k ≤ 0 yields empty list)
     * @return list of suggestions, possibly empty
     * @throws IllegalArgumentException if prefix is null
     */
    public List<String> getTopK(String prefix, int k) {
        if (prefix == null) throw new IllegalArgumentException("prefix cannot be null");
        if (k <= 0) return Collections.emptyList();

        Node node = root;
        for (char ch : prefix.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) return Collections.emptyList();
        }

        List<WordFreq> collected = new ArrayList<>();
        StringBuilder sb = new StringBuilder(prefix);
        collect(node, sb, collected);

        // sort by frequency descending, then lexicographically ascending
        collected.sort((a, b) -> {
            int cmp = Integer.compare(b.freq, a.freq);
            return (cmp != 0) ? cmp : a.word.compareTo(b.word);
        });

        List<String> result = new ArrayList<>(Math.min(k, collected.size()));
        for (int i = 0; i < Math.min(k, collected.size()); i++) {
            result.add(collected.get(i).word);
        }
        return result;
    }

    // Helper class to keep word/frequency pairs.
    private static class WordFreq {
        final String word;
        final int freq;
        WordFreq(String w, int f) { this.word = w; this.freq = f; }
    }

    // Depth‑first traversal collecting all words below {@code node}.
    private void collect(Node node, StringBuilder path, List<WordFreq> out) {
        if (node.isWord) {
            out.add(new WordFreq(path.toString(), node.frequency));
        }
        for (Map.Entry<Character, Node> e : node.children.entrySet()) {
            path.append(e.getKey());
            collect(e.getValue(), path, out);
            path.deleteCharAt(path.length() - 1);
        }
    }
}
