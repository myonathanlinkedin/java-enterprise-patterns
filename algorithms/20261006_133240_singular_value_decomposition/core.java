import java.util.Random;

public class SVD {
    public static class Result {
        public final double[][] U;
        public final double[] S;
        public final double[][] V;
        public Result(double[][] U, double[] S, double[][] V) {
            this.U = U;
            this.S = S;
            this.V = V;
        }
    }

    public static Result computeSVD(double[][] A, int k) {
        int m = A.length;
        int n = A[0].length;
        double[][] ATA = multiply(transpose(A), A);
        double[][] M = copyMatrix(ATA);
        double[][] V = new double[n][k];
        double[] S = new double[k];
        for (int i = 0; i < k; i++) {
            double[] v = powerIteration(M);
            double lambda = dot(v, multiply(M, v));
            double sigma = Math.sqrt(lambda);
            S[i] = sigma;
            V[i] = v;
            double[][] outer = outerProduct(v, v);
            for (int r = 0; r < n; r++)
                for (int c = 0; c < n; c++)
                    M[r][c] -= lambda * outer[r][c];
        }
        double[][] Vmat = new double[n][k];
        for (int i = 0; i < k; i++)
            for (int r = 0; r < n; r++)
                Vmat[r][i] = V[i][r];
        double[][] U = new double[m][k];
        for (int i = 0; i < k; i++) {
            double[] vi = new double[n];
            for (int r = 0; r < n; r++) vi[r] = Vmat[r][i];
            double[] ui = multiply(A, vi);
            double norm = Math.sqrt(dot(ui, ui));
            if (norm > 0)
                for (int r = 0; r < m; r++)
                    U[r][i] = ui[r] / norm;
        }
        return new Result(U, S, Vmat);
    }

    private static double[] powerIteration(double[][] M) {
        int n = M.length;
        double[] b = new double[n];
        Random rnd = new Random(0);
        for (int i = 0; i < n; i++) b[i] = rnd.nextDouble();
        double norm = Math.sqrt(dot(b, b));
        for (int i = 0; i < n; i++) b[i] /= norm;
        double[] bNew = new double[n];
        double eps = 1e-10;
}
}
