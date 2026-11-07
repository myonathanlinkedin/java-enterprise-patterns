import java.util.*;

public class Main {

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed (expected true): " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed (expected false): " + message);
        }
    }

    /** Constructs the example grammar described in the specification:
     *  Non‑terminals: S, A, B
     *  Terminals: a, b
     *  Rules (CNF):
     *      S -> S S
     *      S -> A B
     *      S -> B A
     *      A -> a
     *      B -> b
     *
     *  This grammar generates any even‑length string that can be split into
     *  blocks of "ab" or "ba".
     */
    private static Types.Grammar buildSampleGrammar() {
        Set<String> nonTerminals = new HashSet<>(Arrays.asList("S", "A", "B"));
        Set<String> terminals = new HashSet<>(Arrays.asList("a", "b"));
        List<Types.Rule> rules = new ArrayList<>();
        rules.add(new Types.Rule("S", "S", "S"));
        rules.add(new Types.Rule("S", "A", "B"));
        rules.add(new Types.Rule("S", "B", "A"));
        rules.add(new Types.Rule("A", "a"));
        rules.add(new Types.Rule("B", "b"));
        return new Types.Grammar(nonTerminals, terminals, "S", rules);
    }

    public static void main(String[] args) {
        Types.Grammar grammar = buildSampleGrammar();

        // Positive test cases (should be accepted)
        assertTrue(CYKEngine.parse("ab", grammar), "\"ab\" should be accepted");
        assertTrue(CYKEngine.parse("ba", grammar), "\"ba\" should be accepted");
        assertTrue(CYKEngine.parse("abab", grammar), "\"abab\" should be accepted");
        assertTrue(CYKEngine.parse("baba", grammar), "\"baba\" should be accepted");
        assertTrue(CYKEngine.parse("abba", grammar), "\"abba\" should be accepted");
        assertTrue(CYKEngine.parse("baab", grammar), "\"baab\" should be accepted");
        assertTrue(CYKEngine.parse("ababab", grammar), "\"ababab\" should be accepted");

        // Negative test cases (should be rejected)
        assertFalse(CYKEngine.parse("", grammar), "empty string should be rejected");
        assertFalse(CYKEngine.parse("a", grammar), "\"a\" should be rejected");
        assertFalse(CYKEngine.parse("b", grammar), "\"b\" should be rejected");
        assertFalse(CYKEngine.parse("aa", grammar), "\"aa\" should be rejected");
        assertFalse(CYKEngine.parse("bb", grammar), "\"bb\" should be rejected");
        assertFalse(CYKEngine.parse("aba", grammar), "\"aba\" (odd length) should be rejected");
        assertFalse(CYKEngine.parse("abc", grammar), "\"abc\" contains invalid terminal");
        assertFalse(CYKEngine.parse("abbb", grammar), "\"abbb\" should be rejected");

        // Edge case: longer valid string
        StringBuilder longValid = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longValid.append(i % 2 == 0 ? "ab" : "ba");
        }
        assertTrue(CYKEngine.parse(longValid.toString(), grammar), "Long valid string should be accepted");

        System.out.println("All CYK unit tests passed.");
    }
}
