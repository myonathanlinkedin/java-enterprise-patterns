package cowvector;

public class Main {
    public static void main(String[] args) {
        // Basic functionality test
        CowVector<Integer> v1 = new CowVector<>();
        for (int i = 0; i < 10; i++) {
            v1.push(i);
        }
        assert v1.size() == 10 : "Size should be 10";
        for (int i = 0; i < 10; i++) {
            assert v1.get(i) == i : "Element mismatch at " + i;
        }

        // Share the vector (copy‑on‑write)
        CowVector<Integer> v2 = v1.share();
        assert v2.size() == v1.size() : "Shared vector size mismatch";

        // Verify that reading from shared vector sees same data
        for (int i = 0; i < v2.size(); i++) {
            assert v2.get(i).equals(v1.get(i)) : "Shared read mismatch at " + i;
        }

        // Mutate v2; this should trigger a copy
        v2.set(0, 100);
        assert v2.get(0) == 100 : "v2 modification failed";
        assert v1.get(0) == 0   : "v1 should remain unchanged after v2 modification";

        // Mutate v1; ensure it also copies back to exclusive buffer
        v1.push(999);
        assert v1.size() == 11 : "v1 push after sharing failed";
        assert v1.get(10) == 999 : "v1 new element incorrect";

        // Ensure v2 is unaffected by v1's later mutation
        assert v2.size() == 10 : "v2 size should stay 10";
        assert v2.get(0) == 100 : "v2 first element should stay 100";

        // Test pop operation
        int popped = v1.pop();
        assert popped == 999 : "pop returned wrong value";
        assert v1.size() == 10 : "size after pop incorrect";

        // Edge case: pop until empty and verify exception
        CowVector<String> empty = new CowVector<>();
        boolean caught = false;
        try {
            empty.pop();
        } catch (IllegalStateException e) {
            caught = true;
        }
        assert caught : "Expected exception on pop from empty vector";

        // Edge case: out‑of‑bounds access
        caught = false;
        try {
            v1.get(-1);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assert caught : "Expected exception on negative index";

        caught = false;
        try {
            v1.set(v1.size(), 0);
        } catch (IndexOutOfBoundsException e) {
            caught = true;
        }
        assert caught : "Expected exception on index == size";

        // Verify toArray produces correct snapshot
        Integer[] arr = v1.toArray();
        assert arr.length == v1.size() : "toArray length mismatch";
        for (int i = 0; i < arr.length; i++) {
            assert arr[i].equals(v1.get(i)) : "toArray element mismatch at " + i;
        }

        // Stress test: large number of pushes to trigger multiple resizes
        CowVector<Integer> large = new CowVector<>();
        int N = 1_000_0; // 10,000 elements
        for (int i = 0; i < N; i++) {
            large.push(i);
        }
        assert large.size() == N : "Large vector size mismatch";
        for (int i = 0; i < N; i += 997) { // sample checks
            assert large.get(i) == i : "Large vector element mismatch at " + i;
        }

        // All tests passed
        System.out.println("All Copy‑On‑Write Vector tests passed.");
    }
}
