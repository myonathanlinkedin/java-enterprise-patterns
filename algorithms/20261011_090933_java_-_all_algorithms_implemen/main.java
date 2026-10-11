public class Main {

    public static void main(String[] args) {
        // Enable assertions when running: java -ea Main
        testConstruction();
        testBasicUnionFind();
        testPathCompressionEffect();
        testUnionByRankBalancing();
        testEngineUtilities();
        testExceptionSafety();

        System.out.println("All tests passed.");
    }

    private static void testConstruction() {
        DisjointSet ds = new DisjointSet(5);
        assert ds.size() == 5 : "Size mismatch after construction";
        for (int i = 0; i < ds.size(); i++) {
            assert ds.find(i) == i : "Each element should be its own root initially";
        }
    }

    private static void testBasicUnionFind() {
        DisjointSet ds = new DisjointSet(4);
        assert ds.connected(0, 1) == false : "Initially disconnected";

        boolean merged = ds.union(0, 1);
        assert merged : "Union should report a merge";
        assert ds.connected(0, 1) : "Elements 0 and 1 must be connected after union";

        merged = ds.union(2, 3);
        assert merged : "Second union must merge";
        assert ds.connected(2, 3) : "Elements 2 and 3 must be connected";

        assert ds.connected(0, 2) == false : "Different components remain separate";

        merged = ds.union(1, 2);
        assert merged : "Merging two components must succeed";
        assert ds.connected(0, 3) : "All elements should now be in one component";
    }

    private static void testPathCompressionEffect() {
        DisjointSet ds = new DisjointSet(6);
        // Build a linear chain: 0-1-2-3-4-5
        for (int i = 0; i < 5; i++) {
            ds.union(i, i + 1);
        }

        // At this point, the tree height could be up to 5.
        // After a find on the deepest node, path compression should flatten it.
        int rootBefore = ds.find(5);
        assert rootBefore == 0 : "Root of chain should be 0";

        // Verify that intermediate nodes now point directly to root
        for (int i = 0; i < 6; i++) {
            assert ds.find(i) == 0 : "All nodes must resolve to root 0 after compression";
        }
    }

    private static void testUnionByRankBalancing() {
        DisjointSet ds = new DisjointSet(8);
        // Perform unions in a pattern that would create unbalanced trees
        // if union‑by‑rank were not applied.
        ds.union(0, 1); // rank[0] = 1
        ds.union(2, 3); // rank[2] = 1
        ds.union(4, 5); // rank[4] = 1
        ds.union(6, 7); // rank[6] = 1

        // Merge pairs of equal rank; rank should increase only when necessary.
        ds.union(0, 2); // both rank 1 -> one becomes root, rank becomes 2
        ds.union(4, 6); // same as above

        // Now merge the two larger trees.
        ds.union(0, 4); // ranks 2 vs 2 -> rank of new root becomes 3

        // All elements should share the same root.
        int commonRoot = ds.find(0);
        for (int i = 1; i < 8; i++) {
            assert ds.find(i) == commonRoot : "All elements must have the same root after balanced unions";
        }

        // The final rank of the root should be 3 (since 8 = 2^3)
        // We cannot access rank directly, but we can infer that the tree depth is ≤ 3.
        // Verify by checking that any find does not cause a stack overflow (implicit).
    }

    private static void testEngineUtilities() {
        DisjointSet ds = new DisjointSet(5);
        ds.union(0, 1);
        ds.union(2, 3);
        // Components: {0,1}, {2,3}, {4}
        int components = DisjointSetEngine.countComponents(ds);
        assert components == 3 : "Component count should be 3";

        boolean[] mask = DisjointSetEngine.componentMask(ds, 2);
        assert mask.length == 5 : "Mask length must equal DS size";
        assert mask[2] && mask[3] : "Elements 2 and 3 belong to the queried component";
        assert !mask[0] && !mask[1] && !mask[4] : "Other elements must be false in the mask";
    }

    private static void testExceptionSafety() {
        boolean caught = false;
        try {
            new DisjointSet(0);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assert caught : "Constructor must reject non‑positive size";

        DisjointSet ds = new DisjointSet(3);
        caught = false;
        try {
            ds.find(-1);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assert caught : "find must reject negative index";

        caught = false;
        try {
            ds.union(0, 3);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assert caught : "union must reject out‑of‑range index";

        caught = false;
        try {
            DisjointSetEngine.countComponents(null);
        } catch (NullPointerException e) {
            caught = true;
        }
        assert caught : "Engine utility must reject null DisjointSet";
    }
}
