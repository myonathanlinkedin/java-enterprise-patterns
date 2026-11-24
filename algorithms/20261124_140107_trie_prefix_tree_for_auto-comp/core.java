import java.util.*;

public class Trie {
    private static final int ALPHABET_SIZE = 26;

    private static class Node {
        Node[] children = new Node[ALPHABET_SIZE];
        boolean isWord = false;
        int frequency = 0; // valid only if isWord == true
    }

    private final Node root = new Node();

    /**
     * Inserts a word with its associated frequency.
     * If the word already exists, its frequency is updated to the new value.
     *
     * @param word non‑null, non‑empty lowercase alphabetic string
     * @param frequency non‑negative integer
     * @throws IllegalArgumentException if arguments are invalid
     */
    public void insert(String word, int frequency) {
        validateWord(word);
        if (frequency < 0) {
            throw new IllegalArgumentException("frequency must be non‑negative");
        }
        Node cur = root;
        for (char ch : word.toCharArray()) {
            int idx = ch - 'a';
            if (cur.children[idx] == null) {
                cur.children[idx] = new Node();
            }
            cur = cur.children[idx];
        }
        cur.isWord = true;
        cur.frequency = frequency;
    }

    /**
     * Returns up to k words that start with the given prefix,
     * ordered by descending frequency and then lexicographically.
     *
     * @param prefix non‑null, possibly empty lowercase alphabetic string
     * @param k positive integer; if k <= 0 an empty list is returned
     * @return list of matching words (size ≤ k)
     * @throws IllegalArgumentException if prefix contains invalid characters
     */
    public List<String> getTopK(String prefix, int k) {
        if (k <= 0) {
            return Collections.emptyList();
        }
        validatePrefix(prefix);
        Node node = root;
        for (char ch : prefix.toCharArray()) {
            int idx = ch - 'a';
            if (node.children[idx] == null) {
                return Collections.emptyList();
            }
            node = node.children[idx];
        }

        List<WordFreq> collected = new ArrayList<>();
        collect(node, new StringBuilder(prefix), collected);

        // Sort according to frequency descending, then lexicographically ascending
        collected.sort((a, b) -> {
            int cmp = Integer.compare(b.frequency, a.frequency);
            if (cmp != 0) return cmp;
            return a.word.compareTo(b.word);
        });

        List<String> result = new ArrayList<>(Math.min(k, collected.size()));
        for (int i = 0; i < Math.min(k, collected.size()); i++) {
            result.add(collected.get(i).word);
        }
        return result;
    }

    // Helper data holder
    private static class WordFreq {
        final String word;
        final int frequency;
        WordFreq(String w, int f) { this.word = w; this.frequency = f; }
    }

    // Depth‑first collection of all words under a node
    private void collect(Node node, StringBuilder prefix, List<WordFreq> out) {
        if (node.isWord) {
            out.add(new WordFreq(prefix.toString(), node.frequency));
        }
        for (int i = 0; i < ALPHABET_SIZE; i++) {
            Node child = node.children[i];
            if (child != null) {
                prefix.append((char) ('a' + i));
                collect(child, prefix, out);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }
    }

    // Validation helpers
    private void validateWord(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("word must be non‑null and non‑empty");
        }
        for (char ch : word.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("word must contain only lowercase a‑z");
            }
        }
    }

    private void validatePrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must be non‑null");
        }
        for (char ch : prefix.toCharArray()) {
            if (ch < 'a' || ch > 'z') {
                throw new IllegalArgumentException("prefix must contain only lowercase a‑z");
            }
        }
    }
}
