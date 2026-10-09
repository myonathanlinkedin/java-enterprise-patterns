import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public final class HyperLogLog {
    private final int p;               // precision bits
    private final int m;               // number of registers = 2^p
    private final double alphaMM;      // bias correction constant * m^2
    private final byte[] registers;    // registers storing max rank

    public HyperLogLog(int p) {
        if (p < 4 || p > 16) {
            throw new IllegalArgumentException("Precision p must be in [4,16]");
        }
        this.p = p;
        this.m = 1 << p;
        this.alphaMM = HyperLogLogEngine.computeAlphaMM(this.m);
        this.registers = new byte[this.m];
    }

    // Package‑private accessors for the engine
    int getP() { return p; }
    int getM() { return m; }
    double getAlphaMM() { return alphaMM; }
    byte[] getRegisters() { return registers; }

    /** Add a raw 64‑bit hash value to the sketch. */
    public void addHash(long hash) {
        HyperLogLogEngine.add(this, hash);
    }

    /** Add an arbitrary object; its UTF‑8 string representation is hashed with SHA‑256. */
    public void add(Object o) {
        HyperLogLogEngine.add(this, o);
    }

    /** Return the current cardinality estimate. */
    public double estimate() {
        return HyperLogLogEngine.estimate(this);
    }

    /** Merge another sketch of identical precision into this one (in‑place). */
    public void merge(HyperLogLog other) {
        HyperLogLogEngine.merge(this, other);
    }
}
