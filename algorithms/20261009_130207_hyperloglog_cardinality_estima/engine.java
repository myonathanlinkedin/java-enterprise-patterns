import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public final class HyperLogLogEngine {
    private HyperLogLogEngine() { /* utility class */ }

    /** Compute the bias‑correction constant α·m² for a given register count m. */
    static double computeAlphaMM(int m) {
        switch (m) {
            case 16:  return 0.673 * m * m;
            case 32:  return 0.697 * m * m;
            case 64:  return 0.709 * m * m;
            default:  return (0.7213 / (1 + 1.079 / m)) * m * m;
        }
    }

    /** Hash an arbitrary object using SHA‑256 and return the first 64 bits as a long. */
    static long hashObject(Object o) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(o.toString().getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            long h = 0L;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (digest[i] & 0xffL);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** Core add operation given a pre‑computed 64‑bit hash. */
    static void add(HyperLogLog hll, long hash) {
        int p = hll.getP();
        int idx = (int) (hash >>> (64 - p));          // register index
        long w = hash << p;                           // remaining bits
        // rank = position of first 1-bit in w, counting from 1
        int rank = Long.numberOfLeadingZeros(w) + 1 - p;
        if (rank < 1) rank = 1;
        byte[] regs = hll.getRegisters();
        if (rank > (regs[idx] & 0xFF)) {
            regs[idx] = (byte) rank;
        }
    }

    /** Add an object by hashing it first. */
    static void add(HyperLogLog hll, Object o) {
        long hash = hashObject(o);
        add(hll, hash);
    }

    /** Estimate cardinality from the current register values. */
    static double estimate(HyperLogLog hll) {
        int m = hll.getM();
        double alphaMM = hll.getAlphaMM();
        byte[] regs = hll.getRegisters();

        double sum = 0.0;
        int zeroCount = 0;
        for (byte reg : regs) {
            int value = reg & 0xFF;
            sum += 1.0 / (1L << value);
            if (value == 0) zeroCount++;
        }

        double raw = alphaMM / sum;

        // Small‑range correction (linear counting)
        if (raw <= (5.0 / 2.0) * m) {
            if (zeroCount != 0) {
                return m * Math.log((double) m / zeroCount);
            }
            return raw;
        }

        // Large‑range correction
        double two64 = Math.pow(2.0, 64);
        if (raw > (1.0 / 30.0) * two64) {
            return -two64 * Math.log(1.0 - (raw / two64));
        }

        return raw;
    }

    /** Merge source sketch into target sketch (both must share the same precision). */
    static void merge(HyperLogLog target, HyperLogLog source) {
        if (target.getP() != source.getP()) {
            throw new IllegalArgumentException("Cannot merge HLLs with different precisions");
        }
        byte[] tRegs = target.getRegisters();
        byte[] sRegs = source.getRegisters();
        for (int i = 0; i < tRegs.length; i++) {
            if ((sRegs[i] & 0xFF) > (tRegs[i] & 0xFF)) {
                tRegs[i] = sRegs[i];
            }
        }
    }
}
