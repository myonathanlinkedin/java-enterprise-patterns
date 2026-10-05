import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HamiltonianTester {

    private static final Random RAND = new Random();

    /** Represents a term in a Hamiltonian: a coefficient and the qubits it acts on. */
    private static class Term {
        final double coefficient;
        final int[] qubits; // sorted, unique

        Term(double coefficient, int[] qubits) {
            this.coefficient = coefficient;
            this.qubits = qubits.clone();
        }
    }

    /** Generates a random Hamiltonian with given number of qubits and maximum locality. */
    public static List<Term> generateRandomHamiltonian(int nQubits, int maxLocality, int termCount) {
        if (nQubits <= 0 || maxLocality <= 0 || termCount < 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }
        List<Term> h = new ArrayList<>(termCount);
        for (int i = 0; i < termCount; i++) {
            int locality = 1 + RAND.nextInt(Math.min(maxLocality, nQubits));
            int[] qubits = randomDistinctIndices(nQubits, locality);
            double coeff = RAND.nextDouble() * 2 - 1; // [-1,1]
            h.add(new Term(coeff, qubits));
        }
        return h;
    }

    /** Returns a sorted array of distinct random indices in [0, n). */
    private static int[] randomDistinctIndices(int n, int k) {
        boolean[] used = new boolean[n];
        int[] result = new int[k];
        int filled = 0;
        while (filled < k) {
            int idx = RAND.nextInt(n);
            if (!used[idx]) {
                used[idx] = true;
                result[filled++] = idx;
            }
        }
        java.util.Arrays.sort(result);
        return result;
    }

    /** Checks whether the Hamiltonian respects the given locality bound. */
    public static boolean isLocal(List<Term> hamiltonian, int localityBound) {
        for (Term t : hamiltonian) {
            if (t.qubits.length > localityBound) {
                return false;
            }
        }
        return true;
    }

    /** Computes a simple certification metric: sum of absolute coefficients. */
    public static double certificationMetric(List<Term> hamiltonian) {
        double sum = 0.0;
        for (Term t : hamiltonian) {
            sum += Math.abs(t.coefficient);
        }
        return sum;
    }

    /** Heisenberg limit approximation for the given Hamiltonian size. */
    public static double heisenbergLimit(int termCount) {
        // Ideal scaling: O(sqrt(N)). Constant factor set to 1 for simplicity.
        return Math.sqrt(termCount);
    }

    /** Determines if the certification metric exceeds the Heisenberg limit. */
    public static boolean exceedsHeisenbergLimit(List<Term> hamiltonian) {
        double metric = certificationMetric(hamiltonian);
        double limit = heisenbergLimit(hamiltonian.size());
        return metric > limit;
    }

    /** Unit tests executed from main. */
    private static void runTests() {
        // Test 1: Hamiltonian respecting locality should return true.
        List<Term> h1 = generateRandomHamiltonian(5, 2, 10);
        assert isLocal(h1, 2) : "Test 1 failed: locality violation detected";

        // Test 2: Hamiltonian with a term exceeding locality should return false.
        List<Term> h2 = new ArrayList<>();
        h2.add(new Term(0.5, new int[]{0, 1, 2})); // locality 3
        assert !isLocal(h2, 2) : "Test 2 failed: exceeded locality not detected";

        // Test 3: Certification metric vs Heisenberg limit.
        List<Term> h3 = new ArrayList<>();
        h3.add(new Term(1.0, new int[]{0}));
        h3.add(new Term(1.0, new int[]{1}));
        h3.add(new Term(1.0, new int[]{2}));
        // metric = 3, limit = sqrt(3) ≈1.732, should exceed.
        assert exceedsHeisenbergLimit(h3) : "Test 3 failed: expected exceedance";

        // Test 4: Small coefficients should not exceed limit.
        List<Term> h4 = new ArrayList<>();
        h4.add(new Term(0.1, new int[]{0}));
        h4.add(new Term(0.1, new int[]{1}));
        // metric = 0.2, limit = sqrt(2) ≈1.414, should not exceed.
        assert !exceedsHeisenbergLimit(h4) : "Test 4 failed: unexpected exceedance";

        // Test 5: Edge case zero terms.
        List<Term> h5 = new ArrayList<>();
        assert certificationMetric(h5) == 0.0 : "Test 5 failed: metric not zero";
        assert heisenbergLimit(0) == 0.0 : "Test 5 failed: limit not zero";
        assert !exceedsHeisenbergLimit(h5) : "Test 5 failed: zero Hamiltonian exceedance";

        System.out.println("All tests passed.");
    }

    public static void main(String[] args) {
        runTests();
    }
}
