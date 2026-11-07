package main;

import core.IntroSort;

import java.util.Arrays;
import java.util.Random;

public class Main {

    private static final Random RAND = new Random(0xDEADBEEF);

    public static void main(String[] args) {
        testEmpty();
        testSingleElement();
        testAlreadySorted();
        testReverseSorted();
        testRandomSmall();
        testRandomLarge();
        testDuplicates();
        testCustomComparator();
        System.out.println("All tests passed.");
    }

    private static void assertSorted(Integer[] arr) {
        for (int i = 1; i < arr.length; i++) {
            assert arr[i - 1] <= arr[i] : "Array not sorted at index " + (i - 1);
        }
    }

    private static void testEmpty() {
        Integer[] a = new Integer[0];
        IntroSort.sort(a);
        assert a.length == 0;
    }

    private static void testSingleElement() {
        Integer[] a = {42};
        IntroSort.sort(a);
        assert a[0] == 42;
    }

    private static void testAlreadySorted() {
        Integer[] a = new Integer[100];
        for (int i = 0; i < a.length; i++) a[i] = i;
        IntroSort.sort(a);
        assertSorted(a);
    }

    private static void testReverseSorted() {
        Integer[] a = new Integer[100];
        for (int i = 0; i < a.length; i++) a[i] = 100 - i;
        IntroSort.sort(a);
        assertSorted(a);
    }

    private static void testRandomSmall() {
        Integer[] a = randomArray(57);
        Integer[] expected = a.clone();
        Arrays.sort(expected);
        IntroSort.sort(a);
        assert Arrays.equals(a, expected) : "Small random sort failed";
    }

    private static void testRandomLarge() {
        Integer[] a = randomArray(10_000);
        Integer[] expected = a.clone();
        Arrays.sort(expected);
        IntroSort.sort(a);
        assert Arrays.equals(a, expected) : "Large random sort failed";
    }

    private static void testDuplicates() {
        Integer[] a = new Integer[200];
        for (int i = 0; i < a.length; i++) a[i] = i % 7;
        Integer[] expected = a.clone();
        Arrays.sort(expected);
        IntroSort.sort(a);
        assert Arrays.equals(a, expected) : "Duplicates sort failed";
    }

    private static void testCustomComparator() {
        String[] a = {"apple", "Banana", "cherry", "date", "Elderberry"};
        IntroSort.sort(a, String.CASE_INSENSITIVE_ORDER);
        String[] expected = a.clone();
        Arrays.sort(expected, String.CASE_INSENSITIVE_ORDER);
        assert Arrays.equals(a, expected) : "Custom comparator failed";
    }

    private static Integer[] randomArray(int size) {
        Integer[] arr = new Integer[size];
        for (int i = 0; i < size; i++) {
            arr[i] = RAND.nextInt();
        }
        return arr;
    }
}
