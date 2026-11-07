package core;

import java.util.Comparator;

public final class IntroSort {

    private static final int INSERTION_SORT_THRESHOLD = 16;

    private IntroSort() {
        // Prevent instantiation
    }

    public static <T extends Comparable<? super T>> void sort(T[] a) {
        sort(a, Comparator.naturalOrder());
    }

    public static <T> void sort(T[] a, Comparator<? super T> comp) {
        if (a == null || a.length <= 1) {
            return;
        }
        int maxDepth = (int) (2 * Math.floor(Math.log(a.length) / Math.log(2)));
        introsort(a, 0, a.length - 1, maxDepth, comp);
    }

    private static <T> void introsort(T[] a, int lo, int hi, int depthLimit,
                                      Comparator<? super T> comp) {
        while (hi - lo > INSERTION_SORT_THRESHOLD) {
            if (depthLimit == 0) {
                heapSort(a, lo, hi, comp);
                return;
            }
            depthLimit--;
            int p = partition(a, lo, hi, comp);
            // Recurse on smaller side to keep stack depth O(log n)
            if (p - lo < hi - p) {
                introsort(a, lo, p - 1, depthLimit, comp);
                lo = p + 1;
            } else {
                introsort(a, p + 1, hi, depthLimit, comp);
                hi = p - 1;
            }
        }
        insertionSort(a, lo, hi, comp);
    }

    private static <T> int partition(T[] a, int lo, int hi,
                                     Comparator<? super T> comp) {
        int m = lo + ((hi - lo) >> 1);
        // median-of-three to choose pivot
        if (comp.compare(a[lo], a[m]) > 0) swap(a, lo, m);
        if (comp.compare(a[lo], a[hi]) > 0) swap(a, lo, hi);
        if (comp.compare(a[m], a[hi]) > 0) swap(a, m, hi);
        T pivot = a[m];
        swap(a, m, hi - 1); // hide pivot
        int i = lo;
        int j = hi - 1;
        while (true) {
            while (comp.compare(a[++i], pivot) < 0) { }
            while (comp.compare(a[--j], pivot) > 0) { }
            if (i >= j) break;
            swap(a, i, j);
        }
        swap(a, i, hi - 1); // restore pivot
        return i;
    }

    private static <T> void insertionSort(T[] a, int lo, int hi,
                                          Comparator<? super T> comp) {
        for (int i = lo + 1; i <= hi; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= lo && comp.compare(a[j], key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static <T> void heapSort(T[] a, int lo, int hi,
                                     Comparator<? super T> comp) {
        int n = hi - lo + 1;
        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(a, n, i, lo, comp);
        }
        // One by one extract an element from heap
        for (int i = n - 1; i > 0; i--) {
            swap(a, lo, lo + i);
            heapify(a, i, 0, lo, comp);
        }
    }

    private static <T> void heapify(T[] a, int heapSize, int rootIndex,
                                    int offset,
                                    Comparator<? super T> comp) {
        int largest = rootIndex;
        int left = 2 * rootIndex + 1;
        int right = 2 * rootIndex + 2;

        if (left < heapSize &&
                comp.compare(a[offset + left], a[offset + largest]) > 0) {
            largest = left;
        }
        if (right < heapSize &&
                comp.compare(a[offset + right], a[offset + largest]) > 0) {
            largest = right;
        }
        if (largest != rootIndex) {
            swap(a, offset + rootIndex, offset + largest);
            heapify(a, heapSize, largest, offset, comp);
        }
    }

    private static void swap(Object[] a, int i, int j) {
        Object tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }
}
