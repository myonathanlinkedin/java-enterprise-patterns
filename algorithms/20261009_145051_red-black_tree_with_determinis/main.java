package rbtree;

public class Main {
    public static void main(String[] args) {
        RedBlackTree tree = new RedBlackTree();

        // Simple sequential insertions
        int[] keys = {10, 20, 30, 15, 25, 5, 1, 6, 14, 16};
        for (int i = 0; i < keys.length; i++) {
            tree.insert(keys[i], keys[i] * 10);
            tree.validate(); // Validate after each insertion
        }

        // Verify search and get
        for (int key : keys) {
            assert tree.contains(key) : "Tree should contain key " + key;
            assert tree.get(key) == key * 10 : "Incorrect value for key " + key;
        }

        // Duplicate key insertion should replace value without breaking properties
        tree.insert(15, 999);
        assert tree.get(15) == 999 : "Duplicate insertion failed to replace value";
        tree.validate();

        // Edge cases: inserting minimum and maximum integer values
        tree.insert(Integer.MIN_VALUE, -1);
        tree.insert(Integer.MAX_VALUE, 1);
        assert tree.contains(Integer.MIN_VALUE) : "Missing Integer.MIN_VALUE";
        assert tree.contains(Integer.MAX_VALUE) : "Missing Integer.MAX_VALUE";
        tree.validate();

        // Stress test: insert a large number of sequential keys
        RedBlackTree largeTree = new RedBlackTree();
        final int N = 1000;
        for (int i = 0; i < N; i++) {
            largeTree.insert(i, i);
            largeTree.validate();
        }
        // Verify a few random lookups
        assert largeTree.get(0) == 0;
        assert largeTree.get(N / 2) == N / 2;
        assert largeTree.get(N - 1) == N - 1;

        System.out.println("All Red-Black Tree tests passed.");
    }
}
