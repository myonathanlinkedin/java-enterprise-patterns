public class Main {
    private static final int DEPTH_LIMIT = 1000;

    public static void main(String[] args) {
        testAlwaysSpec();
        testEventuallySpec();
        testUntilSpec();
        testTauHandling();
        testDepthLimit();
        System.out.println("All tests passed.");
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError("FAIL: " + msg);
    }

    private static void assertFalse(boolean cond, String msg) {
        if (cond) throw new AssertionError("FAIL: " + msg);
    }

    /** Build a simple deterministic tree: a -> b -> Return("done") */
    private static InteractionTree<String> simpleTree() {
        return new Vis<>("a",
                new Vis<>("b",
                        new Return<>("done")));
    }

    /** Build a tree with a silent step before the first event. */
    private static InteractionTree<String> tauTree() {
        return new Tau<>(
                new Vis<>("x",
                        new Return<>("end")));
    }

    private static void testAlwaysSpec() {
        InteractionTree<String> t = simpleTree();
        AlwaysSpec<String> alwaysAB = new AlwaysSpec<>(ev -> ev.equals("a") || ev.equals("b"));
        assertTrue(t.satisfies(alwaysAB, DEPTH_LIMIT), "AlwaysAB should hold");

        AlwaysSpec<String> alwaysA = new AlwaysSpec<>(ev -> ev.equals("a"));
        assertFalse(t.satisfies(alwaysA, DEPTH_LIMIT), "AlwaysA should fail on 'b'");
    }

    private static void testEventuallySpec() {
        InteractionTree<String> t = simpleTree();
        EventuallySpec<String> eventuallyB = new EventuallySpec<>(ev -> ev.equals("b"));
        assertTrue(t.satisfies(eventuallyB, DEPTH_LIMIT), "EventuallyB should hold");

        EventuallySpec<String> eventuallyC = new EventuallySpec<>(ev -> ev.equals("c"));
        assertFalse(t.satisfies(eventuallyC, DEPTH_LIMIT), "EventuallyC should fail");
    }

    private static void testUntilSpec() {
        InteractionTree<String> t = simpleTree();
        UntilSpec<String> aUntilB = new UntilSpec<>(ev -> ev.equals("a"), ev -> ev.equals("b"));
        assertTrue(t.satisfies(aUntilB, DEPTH_LIMIT), "aUntilB should hold");

        UntilSpec<String> bUntilA = new UntilSpec<>(ev -> ev.equals("b"), ev -> ev.equals("a"));
        assertFalse(t.satisfies(bUntilA, DEPTH_LIMIT), "bUntilA should fail (first event not b)");
    }

    private static void testTauHandling() {
        InteractionTree<String> t = tauTree();
        EventuallySpec<String> eventuallyX = new EventuallySpec<>(ev -> ev.equals("x"));
        assertTrue(t.satisfies(eventuallyX, DEPTH_LIMIT), "Tau tree should eventually see 'x'");

        AlwaysSpec<String> alwaysX = new AlwaysSpec<>(ev -> ev.equals("x"));
        assertTrue(t.satisfies(alwaysX, DEPTH_LIMIT), "AlwaysX holds because only 'x' appears");
    }

    private static void testDepthLimit() {
        // Construct a long chain of taus ending with a visible event.
        InteractionTree<String> cur = new Return<>("end");
        for (int i = 0; i < 5000; i++) {
            cur = new Tau<>(cur);
        }
        cur = new Vis<>("z", cur);

        EventuallySpec<String> eventuallyZ = new EventuallySpec<>(ev -> ev.equals("z"));
        // With sufficient depth it should succeed.
        assertTrue(cur.satisfies(eventuallyZ, 6000), "Deep tree should succeed with large depth");
        // With insufficient depth it should fail.
        assertFalse(cur.satisfies(eventuallyZ, 100), "Deep tree should fail with small depth");
    }
}
