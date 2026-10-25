class Main {
    private static final double EPS = 1e-8;

    public static void main(String[] args) {
        testIdentity();
        testSymmetric();
        testLowRank();
        System.out.println("All tests passed.");
    }

    private static void testIdentity() {
        double[][] I = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        double[][] recon = SVD.lowRankApprox(I, 3);
        assertMatrixClose(I, recon, EPS, "Identity reconstruction");
    }

    private static void testSymmetric() {
        double[][] A = {{3, 1}, {1, 3}};
        double[][] recon = SVD.lowRankApprox(A, 2);
        assertMatrixClose(A, recon, 1e-6, "Symmetric matrix reconstruction");
    }

    private static void testLowRank() {
        double[][] A = {
                {2, 0},
                {0, 1},
                {1, 0}
        };
        // Rank‑1 approximation should capture the dominant singular direction
        double[][] approx = SVD.lowRankApprox(A, 1);
        double normOriginal = Matrix.frobeniusNorm(A);
        double normApprox = Matrix.frobeniusNorm(approx);
        if (normApprox >= normOriginal) {
            throw new AssertionError("Low‑rank approximation not reducing norm");
        }
        // Verify that error is within expected bound (theoretical bound not needed)
        double[][] diff = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                diff[i][j] = A[i][j] - approx[i][j];
            }
        }
        double err = Matrix.frobeniusNorm(diff);
        if (err > 1.0) {
            throw new AssertionError("Low‑rank approximation error too large: " + err);
        }
    }

    private static void assertMatrixClose(double[][] expected, double[][] actual, double eps, String msg) {
        if (expected.length != actual.length || expected[0].length != actual[0].length) {
            throw new AssertionError(msg + ": dimension mismatch");
        }
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                if (Math.abs(expected[i][j] - actual[i][j]) > eps) {
                    throw new AssertionError(msg + ": element (" + i + "," + j + ") differs. Expected " +
                            expected[i][j] + " but got " + actual[i][j]);
                }
            }
        }
    }
}
