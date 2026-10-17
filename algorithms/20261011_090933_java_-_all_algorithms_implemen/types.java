public class DisjointSet {
    private final int[] parent;
    private final int[] rank;

    /**
     * Constructs a Disjoint Set (Union‑Find) structure with {@code size} singleton sets.
     *
     * @param size number of elements (must be positive)
     * @throws IllegalArgumentException if {@code size} ≤ 0
     */
    public DisjointSet(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;   // each element is its own root
            rank[i] = 0;     // initial rank
        }
    }

    /**
     * Returns the representative (root) of the set containing {@code x}.
     * Implements path compression so that future calls are near‑constant time.
     *
     * @param x element index
     * @return root of the set containing {@code x}
     * @throws IndexOutOfBoundsException if {@code x} is out of range
     */
    public int find(int x) {
        checkIndex(x);
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // recursive path compression
        }
        return parent[x];
    }

    /**
     * Merges the sets containing {@code x} and {@code y}.
     * Uses union by rank to keep trees shallow.
     *
     * @param x first element
     * @param y second element
     * @return {@code true} if a merge happened; {@code false} if already in the same set
     * @throws IndexOutOfBoundsException if either index is out of range
     */
    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false; // already united
        }

        // Union by rank
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++; // increase rank when heights are equal
        }
        return true;
    }

    /**
     * Checks whether {@code x} and {@code y} belong to the same set.
     *
     * @param x first element
     * @param y second element
     * @return {@code true} if both are in the same set
     * @throws IndexOutOfBoundsException if either index is out of range
     */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /**
     * Returns the total number of elements managed by this structure.
     *
     * @return size
     */
    public int size() {
        return parent.length;
    }

    private void checkIndex(int idx) {
        if (idx < 0 || idx >= parent.length) {
            throw new IndexOutOfBoundsException("Index " + idx + " out of bounds");
        }
    }
}
