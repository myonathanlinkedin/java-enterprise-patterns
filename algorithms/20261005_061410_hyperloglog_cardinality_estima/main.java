import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HyperLogLog {
    private final int p; // precision
    private final int m; // number of registers
    private final byte[] registers;
    private static final double[] ALPHA = {
        0, 0, 0, 0, 0.673, 0.697, 0.709, 0.715,
        0.718, 0.720, 0.721, 0.722, 0.723, 0.7235, 0.724, 0.7245
    };
    private static final MessageDigest DIGEST;

    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public HyperLogLog(int p) {
        if (p < 4 || p > 16) throw new IllegalArgumentException("p must be 4..16");
        this.p = p;
        this.m = 1 << p;
        this.registers = new byte[m];
    }

    private static long hash(long value) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) bytes[7 - i] = (byte) (value >>> (i * 8));
        DIGEST.update(bytes);
        byte[] digest = DIGEST.digest();
        long h = 0;
        for (int i = 0; i < 8; i++) h = (h << 8) | (digest[i] & 0xFFL);
        return h;
    }

    private static int leadingZeros(long x, int bits) {
        return Long.numberOfLeadingZeros(x) - (64 - bits);
    }

    public void add(long value) {
        long h = hash(value);
        int idx = (int) (h >>> (64 - p));
        int w = (int) (h << p) | (1 << (p - 1));
        int rank = leadingZeros(w, 64 - p) + 1;
        if (rank > registers[idx]) registers[idx] = (byte) rank;
    }

    public long estimate() {
        double sum = 0.0;
        int zeros = 0;
        for (byte reg : registers) {
            sum += 1.0 / (1L << reg);
            if (reg == 0) zeros++;
        }
        double estimate = getAlpha(m) * m * m / sum;
        if (estimate <= 5.0 / 2.0 * m) {
            if (zeros != 0) estimate = m * Math.log((double) m / zeros);
        } else if (estimate > (1L << 32) / 30.0) {
            estimate = -(1L << 32) * Math.log(1.0 - estimate / (1L << 32));
        }
        return (long) estimate;
    }

    private static double getAlpha(int m) {
        if (m == 16) return ALPHA[4];
        if (m == 32) return ALPHA[5];
        if (m == 64) return ALPHA[6];
        if (m == 128) return ALPHA[7];
        if (m == 256) return ALPHA[8];
        if (m == 512) return ALPHA[9];
        if (m == 1024) return ALPHA[10];
        if (m == 2048) return ALPHA[11];
        if (m == 4096) return ALPHA[12];
        if (m == 8192) return ALPHA[13];
        if (m == 16384) return ALPHA[14];
        if (m == 32768) return ALPHA[15];
        if (m == 65536) return ALPHA[16];
        return 0.7213 / (1 + 1.079 / m);
    }

    // Simple assertion helper
    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError(msg);
    }

    public static void main(String[] args) {
        HyperLogLog hll = new HyperLogLog(14);
        int n = 100_000;
        for (long i = 1; i <= n; i++) hll.add(i);
        long est = hll.estimate();
        System.out.println("Estimated: " + est + ", Actual: " + n);
        assertTrue(Math.abs(est - n) <= n * 0.02, "Error > 2%");

        // Test duplicates
        HyperLogLog dup = new HyperLogLog(14);
        for (int i = 0; i < 50_000; i++) dup.add(42);
        long dupEst = dup.estimate();
        System.out.println("Estimated with duplicates: " + dupEst);
        assertTrue(dupEst == 1, "Duplicate test failed");

        // Random test
        HyperLogLog rand = new HyperLogLog(14);
        java.util.Random r = new java.util.Random(12345);
        java.util.HashSet<Long> set = new java.util.HashSet<>();
        for (int i = 0; i < 200_000; i++) {
            long val = r.nextLong();
            set.add(val);
            rand.add(val);
        }
        long randEst = rand.estimate();
        System.out.println("Random Estimated: " + randEst + ", Actual: " + set.size());
        assertTrue(Math.abs(randEst - set.size()) <= set.size() * 0.02, "Random error > 2%");

        System.out.println("All tests passed.");
    }
}
