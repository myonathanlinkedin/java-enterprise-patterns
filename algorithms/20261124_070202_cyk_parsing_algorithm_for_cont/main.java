package main;

import core.CYK;
import core.CYK.Grammar;
import java.util.*;

public class Main {

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError("Expected true: " + msg);
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) throw new AssertionError("Expected false: " + msg);
    }

    /** Builds the classic example grammar from Wikipedia (CNF). */
    private static Grammar buildExampleGrammar() {
        Grammar g = new Grammar();
        g.setStartSymbol("S");
        // S → AB | BC
        g.addBinary("S", "A", "B");
        g.addBinary("S", "B", "C");
        // A → BA | a
        g.addBinary("A", "B", "A");
        g.addTerminal("A", "a");
        // B → CC | b
        g.addBinary("B", "C", "C");
        g.addTerminal("B", "b");
        // C → AB | a
        g.addBinary("C", "A", "B");
        g.addTerminal("C", "a");
        return g;
    }

    /** Runs a suite of deterministic unit tests. */
    private static void runTests() {
        Grammar g = buildExampleGrammar();

        // Positive examples (should be accepted)
        assertTrue(CYK.parse("baaba", g), "baaba should be in the language");
        assertTrue(CYK.parse("baab", g), "baab should be in the language");
        assertTrue(CYK.parse("aab", g), "aab should be in the language");
        assertTrue(CYK.parse("ab", g), "ab should be in the language");

        // Negative examples (should be rejected)
        assertFalse(CYK.parse("bb", g), "bb should NOT be in the language");
        assertFalse(CYK.parse("aa", g), "aa should NOT be in the language");
        assertFalse(CYK.parse("baba", g), "baba should NOT be in the language");
        assertFalse(CYK.parse("", g), "empty string should NOT be in the language");

        // Edge cases
        Grammar singleTerm = new Grammar();
        singleTerm.setStartSymbol("S");
        singleTerm.addTerminal("S", "x");
        assertTrue(CYK.parse("x", singleTerm), "single terminal x should be accepted");
        assertFalse(CYK.parse("y", singleTerm), "different terminal y should be rejected");

        // Larger random test (deterministic)
        Grammar simple = new Grammar();
        simple.setStartSymbol("S");
        simple.addBinary("S", "S", "S");
        simple.addTerminal("S", "a");
        // Language: strings of a's whose length is a power of two.
        assertTrue(CYK.parse("aa", simple), "aa (2) accepted");
        assertTrue(CYK.parse("aaaa", simple), "aaaa (4) accepted");
        assertFalse(CYK.parse("aaa", simple), "aaa (3) rejected");
        assertFalse(CYK.parse("aaaaa", simple), "aaaaa (5) rejected");
    }

    public static void main(String[] args) {
        runTests();
        System.out.println("All CYK tests passed.");
    }
}
