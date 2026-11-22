public class Main {
    public static void main(String[] args) {
        runAllTests();
    }

    private static void runAllTests() {
        testAllCommit();
        testAbortDueToParticipantVote();
        testAbortDueToParticipantFailure();
        System.out.println("All tests passed.");
    }

    /**
     * Test case where every participant votes COMMIT.
     * Expected outcome: global COMMIT and all participants end in COMMITTED state.
     */
    private static void testAllCommit() {
        SimpleParticipant p1 = new SimpleParticipant(true);
        SimpleParticipant p2 = new SimpleParticipant(true);
        SimpleParticipant p3 = new SimpleParticipant(true);
        Coordinator coord = new Coordinator(List.of(p1, p2, p3));

        Decision d = coord.executeTransaction("tx1");
        assert d == Decision.COMMIT : "Decision should be COMMIT";

        for (Participant p : coord.getParticipants()) {
            assert p.getState() == ParticipantState.COMMITTED
                : "Participant should be COMMITTED, found " + p.getState();
        }
    }

    /**
     * Test case where one participant votes ABORT.
     * Expected outcome: global ABORT and all participants end in ABORTED state.
     */
    private static void testAbortDueToParticipantVote() {
        SimpleParticipant p1 = new SimpleParticipant(true);
        SimpleParticipant p2 = new SimpleParticipant(false); // will vote ABORT
        SimpleParticipant p3 = new SimpleParticipant(true);
        Coordinator coord = new Coordinator(List.of(p1, p2, p3));

        Decision d = coord.executeTransaction("tx2");
        assert d == Decision.ABORT : "Decision should be ABORT";

        for (Participant p : coord.getParticipants()) {
            assert p.getState() == ParticipantState.ABORTED
                : "Participant should be ABORTED, found " + p.getState();
        }
    }

    /**
     * Test case where a participant throws during prepare.
     * Expected outcome: global ABORT and all participants end in ABORTED state.
     */
    private static void testAbortDueToParticipantFailure() {
        SimpleParticipant p1 = new SimpleParticipant(true);
        FaultyParticipant p2 = new FaultyParticipant(); // throws on prepare
        SimpleParticipant p3 = new SimpleParticipant(true);
        Coordinator coord = new Coordinator(List.of(p1, p2, p3));

        Decision d = coord.executeTransaction("tx3");
        assert d == Decision.ABORT : "Decision should be ABORT due to failure";

        // Verify that non‑faulty participants received abort.
        assert p1.getState() == ParticipantState.ABORTED
            : "p1 should be ABORTED after coordinator abort";
        assert p3.getState() == ParticipantState.ABORTED
            : "p3 should be ABORTED after coordinator abort";

        // Faulty participant may remain in its initial state or be set to ABORTED.
        // Our implementation sets it to ABORTED in the abort phase.
        assert p2.getState() == ParticipantState.ABORTED
            : "p2 should be ABORTED after coordinator abort";
    }
}
