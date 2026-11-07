import java.util.*;

public class Types {

    /** Represents a production rule in Chomsky Normal Form.
     *  For terminal rules, right side length is 1 and contains the terminal symbol.
     *  For binary rules, right side length is 2 and contains two non‑terminal symbols.
     */
    public static class Rule {
        public final String left;          // non‑terminal on the left side
        public final List<String> right;   // right side symbols (size 1 or 2)

        public Rule(String left, String... right) {
            if (right == null || right.length == 0 || right.length > 2) {
                throw new IllegalArgumentException("Right side must contain 1 or 2 symbols");
            }
            this.left = Objects.requireNonNull(left);
            this.right = Collections.unmodifiableList(Arrays.asList(right.clone()));
        }

        public boolean isTerminal() {
            return right.size() == 1 && !right.get(0).matches("[A-Z]"); // terminals are lower‑case strings
        }

        @Override
        public String toString() {
            return left + " -> " + String.join(" ", right);
        }
    }

    /** Immutable representation of a context‑free grammar in CNF. */
    public static class Grammar {
        public final Set<String> nonTerminals; // e.g., {"S","A","B"}
        public final Set<String> terminals;    // e.g., {"a","b"}
        public final String startSymbol;       // e.g., "S"
        public final List<Rule> rules;         // all production rules

        public Grammar(Set<String> nonTerminals,
                       Set<String> terminals,
                       String startSymbol,
                       List<Rule> rules) {
            this.nonTerminals = Collections.unmodifiableSet(new HashSet<>(nonTerminals));
            this.terminals = Collections.unmodifiableSet(new HashSet<>(terminals));
            this.startSymbol = Objects.requireNonNull(startSymbol);
            this.rules = Collections.unmodifiableList(new ArrayList<>(rules));

            // basic sanity checks
            if (!this.nonTerminals.contains(startSymbol)) {
                throw new IllegalArgumentException("Start symbol must be a declared non‑terminal");
            }
            for (Rule r : this.rules) {
                if (!this.nonTerminals.contains(r.left)) {
                    throw new IllegalArgumentException("Rule left side must be a non‑terminal: " + r);
                }
                for (String sym : r.right) {
                    if (r.isTerminal()) {
                        if (!this.terminals.contains(sym)) {
                            throw new IllegalArgumentException("Terminal symbol not declared: " + sym);
                        }
                    } else {
                        if (!this.nonTerminals.contains(sym)) {
                            throw new IllegalArgumentException("Non‑terminal symbol not declared: " + sym);
                        }
                    }
                }
            }
        }
    }
}
