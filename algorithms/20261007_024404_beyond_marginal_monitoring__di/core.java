import java.util.*;

public class Pair {
    public final int first;
    public final int second;
    public Pair(int first, int second) {
        this.first = first;
        this.second = second;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair p = (Pair) o;
        return first == p.first && second == p.second;
    }
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}

class JointDistribution {
    private final Map<Pair, Integer> counts = new HashMap<>();
    private int totalCount = 0;

    public void update(int a, int b) {
        Pair p = new Pair(a, b);
        counts.put(p, counts.getOrDefault(p, 0) + 1);
        totalCount++;
    }

    public int getCount(int a, int b) {
        return counts.getOrDefault(new Pair(a, b), 0);
    }

    public int getTotalCount() {
        return totalCount;
    }

    public Set<Pair> getPairs() {
        return counts.keySet();
    }

    public Map<Pair, Integer> getCounts() {
        return Collections.unmodifiableMap(counts);
    }
}

class DriftDetector {
    private final JointDistribution reference;
    private final JointDistribution current = new JointDistribution();
    private final double alpha;

    public DriftDetector(JointDistribution reference, double alpha) {
        this.reference = reference;
        this.alpha = alpha;
    }

    public void update(int a, int b) {
        current.update(a, b);
    }

    public boolean detect() {
        double chi2 = computeChiSquared();
        int df = getDegreesOfFreedom();
        double critical = chi2Critical(df, alpha);
        return chi2 > critical;
    }

    public double computeChiSquared() {
        double chi2 = 0.0;
        int refTotal = reference.getTotalCount();
        int curTotal = current.getTotalCount();
        if (refTotal == 0 || curTotal == 0) return 0.0;
        for (Pair p : reference.getPairs()) {
            int exp = (int) Math.round((double) reference.getCount(p.first, p.second) / refTotal * curTotal);
            if (exp == 0) continue;
            int obs = current.getCount(p.first, p.second);
            double diff = obs - exp;
            chi2 += diff * diff / exp;
        }
        return chi2;
    }

    private int getDegreesOfFreedom() {
        return reference.getPairs().size() - 1;
    }

    private double chi2Critical(double df, double alpha) {
        // Approximation: chi2 = df + 2*sqrt(df)*z + 2*z^2
        double z = inverseStandardNormal(1 - alpha);
        return df + 2 * Math.sqrt(df) * z + 2 * z * z;
    }

    // Approximate inverse standard normal using rational approximation (Abramowitz & Stegun)
    private double inverseStandardNormal(double p) {
        if (p <= 0.0 || p >= 1.0) throw new IllegalArgumentException("p must be in (0,1)");
        double a1 = -39.6968302866538;
        double a2 = 220.946098424521;
        double a3 = -275.928510446969;
        double a4 = 138.357751867269;
        double a5 = -30.6647980661472;
        double a6 = 2.50662827745924;
        double b1 = -54.4760987982241;
        double b2 = 161.585836858041;
        double b3 = -155.698979859887;
        double b4 = 66.8013118877197;
        double b5 = -13.2806815528857;
        double c1 = -0.00778489400243029;
        double c2 = -0.322396458041136;
        double c3 = -2.40075827716184;
        double c4 = -2.54973253934373;
        double c5 = 4.37466414146497;
        double c6 = 2.93816398269878;
        double d1 = 0.00778469570904146;
        double d2 = 0.32246712907004;
        double d3 = 2.445134137143;
        double d4 = 3.75440866190742;
        double q, r, x;
        if (p < 0.02425) {
            q = Math.sqrt(-2 * Math.log(p));
            x = (((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                ((((d1 * q + d2) * q + d3) * q + d4) * q + 1);
        } else if (p > 0.97575) {
            q = Math.sqrt(-2 * Math.log(1 - p));
            x = -(((((c1 * q + c2) * q + c3) * q + c4) * q + c5) * q + c6) /
                ((((d1 * q + d2) * q + d3) * q + d4) * q + 1);
        } else {
            q = p - 0.5;
            r = q * q;
            x = (((((a1 * r + a2) * r + a3) * r + a4) * r + a5) * r + a6) * q /
                (((((b1 * r + b2) * r + b3) * r + b4) * r + b5) * r + 1);
        }
        return x;
    }
}
