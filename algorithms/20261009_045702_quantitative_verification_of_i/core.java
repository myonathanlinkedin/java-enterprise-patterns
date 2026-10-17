import java.util.*;

public class core {

    // Transition of a Petri net: pre-conditions (consumption) and post-conditions (production)
    public static class Transition {
        private final long[] pre;
        private final long[] post;

        public Transition(long[] pre, long[] post) {
            if (pre.length != post.length) {
                throw new IllegalArgumentException("Pre and post vectors must have same length");
            }
            this.pre = pre.clone();
            this.post = post.clone();
        }

        // Returns a new marking after firing, or null if not enabled
        public long[] fire(long[] marking) {
            if (!isEnabled(marking)) {
                return null;
            }
            long[] next = marking.clone();
            for (int i = 0; i < next.length; i++) {
                if (next[i] != OMEGA) {
                    next[i] = next[i] - pre[i] + post[i];
                }
                // ω stays ω
            }
            return next;
        }

        private boolean isEnabled(long[] marking) {
            for (int i = 0; i < marking.length; i++) {
                long m = marking[i];
                long need = pre[i];
                if (m == OMEGA) {
                    continue; // ω can satisfy any finite need
                }
                if (m < need) {
                    return false;
                }
            }
            return true;
        }
    }

    // Petri net definition
    public static class PetriNet {
        private final int placeCount;
        private final List<Transition> transitions = new ArrayList<>();

        public PetriNet(int placeCount) {
            if (placeCount <= 0) {
                throw new IllegalArgumentException("Place count must be positive");
            }
            this.placeCount = placeCount;
        }

        public void addTransition(long[] pre, long[] post) {
            if (pre.length != placeCount || post.length != placeCount) {
                throw new IllegalArgumentException("Vector size mismatch");
            }
            transitions.add(new Transition(pre, post));
        }

        public List<Transition> getTransitions() {
            return Collections.unmodifiableList(transitions);
        }

        public int getPlaceCount() {
            return placeCount;
        }
    }

    // Special constant representing ω (infinite)
    public static final long OMEGA = -1L;

    // Utility methods for vector comparisons handling ω
    private static boolean lessOrEqual(long[] a, long[] b) {
        for (int i = 0; i < a.length; i++) {
            long av = a[i];
            long bv = b[i];
            if (av == OMEGA) {
                if (bv != OMEGA) {
                    return false;
                }
            } else {
                if (bv == OMEGA) {
                    continue;
                }
                if (av > bv) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean equalMarking(long[] a, long[] b) {
        return Arrays.equals(a, b);
    }

    // Node of the coverability tree
    private static class Node {
        final long[] marking;
        final Node parent;
        final List<Node> children = new ArrayList<>();

        Node(long[] marking, Node parent) {
            this.marking = marking;
            this.parent = parent;
        }
    }

    // Karp‑Miller coverability tree builder and query
    public static class CoverabilityTree {
        private final PetriNet net;
        private final Node root;

        public CoverabilityTree(PetriNet net, long[] initialMarking) {
            this.net = net;
            this.root = new Node(initialMarking.clone(), null);
            build();
        }

        // Build the tree using the Karp‑Miller algorithm
        private void build() {
            Deque<Node> stack = new ArrayDeque<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                Node node = stack.pop();
                for (Transition t : net.getTransitions()) {
                    long[] fired = t.fire(node.marking);
                    if (fired == null) {
                        continue; // transition not enabled
                    }
                    // Acceleration step
                    long[] accelerated = accelerate(fired, node);
                    // Avoid duplicate children
                    if (hasChildWithMarking(node, accelerated)) {
                        continue;
                    }
                    Node child = new Node(accelerated, node);
                    node.children.add(child);
                    stack.push(child);
                }
            }
        }

        // Apply acceleration w.r.t. any ancestor that is ≤ the new marking
        private long[] accelerate(long[] marking, Node node) {
            long[] result = marking.clone();
            Node anc = node;
            while (anc != null) {
                if (lessOrEqual(anc.marking, result) && !equalMarking(anc.marking, result)) {
                    for (int i = 0; i < result.length; i++) {
                        long ancVal = anc.marking[i];
                        long curVal = result[i];
                        if (ancVal != OMEGA && curVal != OMEGA && curVal > ancVal) {
                            result[i] = OMEGA;
                        }
                    }
                }
                anc = anc.parent;
            }
            return result;
        }

        private boolean hasChildWithMarking(Node node, long[] marking) {
            for (Node child : node.children) {
                if (equalMarking(child.marking, marking)) {
                    return true;
                }
            }
            return false;
        }

        // Public query: does there exist a node covering the target marking?
        public boolean isCoverable(long[] target) {
            return dfsCover(root, target);
        }

        private boolean dfsCover(Node node, long[] target) {
            if (covers(node.marking, target)) {
                return true;
            }
            for (Node child : node.children) {
                if (dfsCover(child, target)) {
                    return true;
                }
            }
            return false;
        }

        // nodeMarking covers target if for every place nodeMarking >= target (ω counts as infinite)
        private boolean covers(long[] nodeMarking, long[] target) {
            for (int i = 0; i < nodeMarking.length; i++) {
                long nm = nodeMarking[i];
                long tg = target[i];
                if (nm == OMEGA) {
                    continue;
                }
                if (nm < tg) {
                    return false;
                }
            }
            return true;
        }
    }
}
