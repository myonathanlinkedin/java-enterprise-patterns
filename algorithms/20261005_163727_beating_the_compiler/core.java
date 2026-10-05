import java.util.*;

public class SubsetSumSolver {

    private static class Pair {
        final long sum;
        final int mask;
        Pair(long sum, int mask) {
            this.sum = sum;
            this.mask = mask;
        }
    }

    /**
     * Finds a subset of indices (1‑based) whose corresponding values sum to {@code target}.
     * Returns {@code null} if no such subset exists.
     *
     * @param a      array of positive integers
     * @param target desired sum
     * @return array of 1‑based indices or {@code null}
     */
    public static int[] findSubset(int[] a, int target) {
        if (target == 0) return new int[0];
        int n = a.length;
        int leftSize = n / 2;
        int rightSize = n - leftSize;

        List<Pair> left = new ArrayList<>(1 << leftSize);
        for (int mask = 0; mask < (1 << leftSize); mask++) {
            long sum = 0;
            for (int i = 0; i < leftSize; i++) {
                if ((mask & (1 << i)) != 0) sum += a[i];
            }
            left.add(new Pair(sum, mask));
        }

        List<Pair> right = new ArrayList<>(1 << rightSize);
        for (int mask = 0; mask < (1 << rightSize); mask++) {
            long sum = 0;
            for (int i = 0; i < rightSize; i++) {
                if ((mask & (1 << i)) != 0) sum += a[leftSize + i];
            }
            right.add(new Pair(sum, mask));
        }

        right.sort(Comparator.comparingLong(p -> p.sum));

        for (Pair lp : left) {
            long need = (long) target - lp.sum;
            int idx = Collections.binarySearch(right, new Pair(need, 0),
                    Comparator.comparingLong(p -> p.sum));
            if (idx >= 0) {
                Pair rp = right.get(idx);
                int totalSize = Integer.bitCount(lp.mask) + Integer.bitCount(rp.mask);
                int[] result = new int[totalSize];
                int pos = 0;
                for (int i = 0; i < leftSize; i++) {
                    if ((lp.mask & (1 << i)) != 0) result[pos++] = i + 1;
                }
                for (int i = 0; i < rightSize; i++) {
                    if ((rp.mask & (1 << i)) != 0) result[pos++] = leftSize + i + 1;
                }
                return result;
            }
        }
        return null;
    }
}
