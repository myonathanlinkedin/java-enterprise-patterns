package main;

import core.CountMinSketch;
import java.util.*;

public class Main {
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testBasicCounting() {
        CountMinSketch<String> cms = new CountMinSketch<>(5, 1000, 42L);
        cms.add("apple");
        cms.add("banana", 3);
        cms.add("apple", 2);
        assertTrue(cms.estimate("apple") == 3L, "Apple count should be 3");
        assertTrue(cms.estimate("banana") == 3L, "Banana count should be 3");
        assertTrue(cms.estimate("cherry") == 0L, "Unseen item should be 0");
    }

    private static void testHeavyHitters() {
        CountMinSketch<Integer> cms = new CountMinSketch<>(4, 500, 123L);
        // Insert frequencies: 1->5, 2->3, 3->1, 4->7
        for (int i = 0; i < 5; i++) cms.add(1);
        for (int i = 0; i < 3; i++) cms.add(2);
        for (int i = 0; i < 1; i++) cms.add(3);
        for (int i = 0; i < 7; i++) cms.add(4);

        Set<Integer> heavy = cms.heavyHitters(4);
        assertTrue(heavy.contains(1), "1 should be heavy");
        assertTrue(heavy.contains(4), "4 should be heavy");
        assertTrue(!heavy.contains(2), "2 should not be heavy");
        assertTrue(!heavy.contains(3), "3 should not be heavy");
        assertTrue(heavy.size() == 2, "Exactly two heavy hitters expected");
    }

    private static void testInvalidParameters() {
        boolean caught = false;
        try {
            new CountMinSketch<String>(0, 10, 0);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Zero depth must throw");

        caught = false;
        try {
            new CountMinSketch<String>(5, -1, 0);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Negative width must throw");
    }

    private static void testDeterminism() {
        CountMinSketch<String> cms1 = new CountMinSketch<>(3, 200, 999L);
        CountMinSketch<String> cms2 = new CountMinSketch<>(3, 200, 999L);
        String[] items = {"x", "y", "z", "x", "y", "x"};
        for (String s : items) {
            cms1.add(s);
            cms2.add(s);
        }
        for (String s : new String[]{"x","y","z"}) {
            assertTrue(cms1.estimate(s) == cms2.estimate(s),
                    "Estimates must match for same seed and updates");
        }
    }

    public static void main(String[] args) {
        System.out.println("Running Count‑Min Sketch unit tests...");
        testBasicCounting();
        testHeavyHitters();
        testInvalidParameters();
        testDeterminism();
        System.out.println("All tests passed.");
    }
}
