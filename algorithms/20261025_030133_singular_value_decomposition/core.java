import java.util.Arrays;

class Matrix {
    static double[][] copy(double[][] a) {
        int m = a.length;
        int n = a[0].length;
        double[][] r = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(a[i], 0, r[i], 0, n);
        }
        return r;
    }

    static double[][] transpose(double[][] a) {
        int m = a.length;
        int n = a[0].length;
        double[][] r = new double[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                r[j][i] = a[i][j];
            }
        }
        return r;
    }

    static double[][] multiply(double[][] a, double[][] b) {
        int m = a.length;
        int p = a[0].length;
        int n = b[0].length;
        double[][] r = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < p; k++) {
                double aik = a[i][k];
                for (int j = 0; j < n; j++) {
                    r[i][j] += aik * b[k][j];
                }
            }
        }
        return r;
    }

    static double[][] multiply(double[][] a, double[] v) {
        int m = a.length;
        int n = a[0].length;
        double[] r = new double[m];
        for (int i = 0; i < m; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                sum += a[i][j] * v[j];
            }
            r[i] = sum;
        }
        return new double[][]{r};
    }

    static double frobeniusNorm(double[][] a) {
        double sum = 0.0;
        for (double[] row : a) {
            for (double v : row) {
                sum += v * v;
            }
        }
        return Math.sqrt(sum);
    }
}

class JacobiEigen {
    static class Result {
        final double[] eigenvalues;
        final double[][] eigenvectors; // columns are eigenvectors

        Result(double[] eigenvalues, double[][] eigenvectors) {
            this.eigenvalues = eigenvalues;
            this.eigenvectors = eigenvectors;
        }
    }

    static Result decompose(double[][] a) {
        int n = a.length;
        double[][] v = new double[n][n];
        for (int i = 0; i < n; i++) v[i][i] = 1.0;
        double[][] aCopy = Matrix.copy(a);
        final double eps = 1e-12;
        final int maxIter = 100;
        for (int iter = 0; iter < maxIter; iter++) {
            // find largest off-diagonal element
            int p = 0, q = 1;
            double max = Math.abs(aCopy[p][q]);
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    double abs = Math.abs(aCopy[i][j]);
                    if (abs > max) {
                        max = abs;
                        p = i;
                        q = j;
                    }
                }
            }
            if (max < eps) break;
            double phi = 0.5 * Math.atan2(2.0 * aCopy[p][q], aCopy[q][q] - aCopy[p][p]);
            double c = Math.cos(phi);
            double s = Math.sin(phi);
            // rotate rows/cols p and q
            for (int i = 0; i < n; i++) {
                double api = aCopy[i][p];
                double aqi = aCopy[i][q];
                aCopy[i][p] = c * api - s * aqi;
                aCopy[i][q] = s * api + c * aqi;
            }
            for (int i = 0; i < n; i++) {
                double aip = aCopy[p][i];
                double aiq = aCopy[q][i];
                aCopy[p][i] = c * aip - s * aiq;
                aCopy[q][i] = s * aip + c * aiq;
            }
            // enforce symmetry
            for (int i = 0; i < n; i++) {
                aCopy[p][i] = aCopy[i][p];
                aCopy[q][i] = aCopy[i][q];
            }
            // update eigenvectors
            for (int i = 0; i < n; i++) {
                double vip = v[i][p];
                double viq = v[i][q];
                v[i][p] = c * vip - s * viq;
                v[i][q] = s * vip + c * viq;
            }
        }
        double[] eigenvalues = new double[n];
        for (int i = 0; i < n; i++) eigenvalues[i] = aCopy[i][i];
        // sort descending
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, (i, j) -> Double.compare(eigenvalues[j], eigenvalues[i]));
        double[] sortedVals = new double[n];
        double[][] sortedVecs = new double[n][n];
        for (int i = 0; i < n; i++) {
            sortedVals[i] = eigenvalues[idx[i]];
            for (int j = 0; j < n; j++) {
                sortedVecs[j][i] = v[j][idx[i]];
            }
        }
        return new Result(sortedVals, sortedVecs);
    }
}

class SVD {
    static class Result {
        final double[][] U; // m x r
        final double[] S;   // r singular values (sorted descending)
        final double[][] V; // n x r (columns)

        Result(double[][] U, double[] S, double[][] V) {
            this.U = U;
            this.S = S;
            this.V = V;
        }
    }

    static Result compute(double[][] A) {
        int m = A.length;
        int n = A[0].length;
        double[][] At = Matrix.transpose(A);
        double[][] AtA = Matrix.multiply(At, A); // n x n symmetric
        JacobiEigen.Result eig = JacobiEigen.decompose(AtA);
        double[] sigma = new double[n];
        double[][] V = new double[n][n];
        for (int i = 0; i < n; i++) {
            sigma[i] = Math.sqrt(Math.max(eig.eigenvalues[i], 0.0));
            for (int j = 0; j < n; j++) V[j][i] = eig.eigenvectors[j][i];
        }
        // compute U = A * V * Σ^{-1}
        double[][] U = new double[m][n];
        for (int i = 0; i < n; i++) {
            if (sigma[i] > 1e-12) {
                double inv = 1.0 / sigma[i];
                for (int row = 0; row < m; row++) {
                    double sum = 0.0;
                    for (int col = 0; col < n; col++) {
                        sum += A[row][col] * V[col][i];
                    }
                    U[row][i] = sum * inv;
                }
            } else {
                // zero singular value: fill with orthogonal vector via Gram-Schmidt
                // simple fallback: set column to zero
                for (int row = 0; row < m; row++) U[row][i] = 0.0;
            }
        }
        // truncate to rank r = number of non‑zero singular values
        int r = 0;
        for (double s : sigma) if (s > 1e-12) r++;
        double[][] Utrim = new double[m][r];
        double[] Strim = new double[r];
        double[][] Vtrim = new double[n][r];
        for (int i = 0; i < r; i++) {
            Strim[i] = sigma[i];
            for (int row = 0; row < m; row++) Utrim[row][i] = U[row][i];
            for (int row = 0; row < n; row++) Vtrim[row][i] = V[row][i];
        }
        return new Result(Utrim, Strim, Vtrim);
    }

    static double[][] lowRankApprox(double[][] A, int k) {
        Result svd = compute(A);
        int rank = Math.min(k, svd.S.length);
        double[][] approx = new double[A.length][A[0].length];
        for (int i = 0; i < rank; i++) {
            double sigma = svd.S[i];
            double[] uCol = new double[svd.U.length];
            double[] vCol = new double[svd.V.length];
            for (int r = 0; r < svd.U.length; r++) uCol[r] = svd.U[r][i];
            for (int c = 0; c < svd.V.length; c++) vCol[c] = svd.V[c][i];
            for (int r = 0; r < approx.length; r++) {
                for (int c = 0; c < approx[0].length; c++) {
                    approx[r][c] += sigma * uCol[r] * vCol[c];
                }
            }
        }
        return approx;
    }
}
