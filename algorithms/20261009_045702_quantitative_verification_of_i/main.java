public class main {
    private static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    // Helper to create a vector of given length filled with zeros
    private static long[] zeros(int n) {
        return new long[n];
    }

    // Test 1: Self‑loop transition creates unbounded tokens, target reachable
    private static void testUnboundedSelfLoop() {
        core.PetriNet net = new core.PetriNet(1);
        // Transition: consumes 0, produces 1 token in place 0
        net.addTransition(new long[]{0}, new long[]{1});

        long[] init = new long[]{0};
        core.CoverabilityTree tree = new core.CoverabilityTree(net, init);

        long[] target = new long[]{5};
        assertTrue(tree.isCoverable(target), "Self‑loop should cover 5 tokens");
    }

    // Test 2: Simple token transfer, reachable target
    private static void testTokenTransfer() {
        core.PetriNet net = new core.PetriNet(2);
        // Transition: move one token from place0 to place1
        net.addTransition(new long[]{1,0}, new long[]{0,1});

        long[] init = new long[]{1,0};
        core.CoverabilityTree tree = new core.CoverabilityTree(net, init);

        long[] target = new long[]{0,1};
        assertTrue(tree.isCoverable(target), "Token should be transferable to place1");
    }

    // Test 3: Unreachable target
    private static void testUnreachable() {
        core.PetriNet net = new core.PetriNet(1);
        // No transitions at all
        long[] init = new long[]{0};
        core.CoverabilityTree tree = new core.CoverabilityTree(net, init);

        long[] target = new long[]{1};
        assertTrue(!tree.isCoverable(target), "Without transitions target should be unreachable");
    }

    // Test 4: Acceleration creates ω correctly
    private static void testAcceleration() {
        core.PetriNet net = new core.PetriNet(2);
        // t1: add token to p0
        net.addTransition(new long[]{0,0}, new long[]{1,0});
        // t2: copy token from p0 to p1 (consume 1 from p0, produce 1 in p1)
        net.addTransition(new long[]{1,0}, new long[]{0,1});

        long[] init = new long[]{0,0};
        core.CoverabilityTree tree = new core.CoverabilityTree(net, init);

        // After arbitrarily many t1 firings, p0 becomes ω, then t2 can produce arbitrarily many in p1
        long[] target = new long[]{core.OMEGA, 10};
        assertTrue(tree.isCoverable(target), "Acceleration should allow arbitrarily many tokens in p1");
    }

    public static void main(String[] args) {
        testUnboundedSelfLoop();
        testTokenTransfer();
        testUnreachable();
        testAcceleration();
        System.out.println("All tests passed.");
    }
}
