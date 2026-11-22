import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Vote options a participant can return during the prepare phase.
 */
enum Vote {
    COMMIT,
    ABORT
}

/**
 * Final decision made by the coordinator after collecting votes.
 */
enum Decision {
    COMMIT,
    ABORT
}

/**
 * Internal state of a participant in the 2PC protocol.
 */
enum ParticipantState {
    INIT,       // No transaction seen yet
    PREPARED,   // Voted COMMIT, ready to commit
    COMMITTED,  // Transaction committed
    ABORTED     // Transaction aborted
}

/**
 * Participant interface defining the 2PC state machine.
 */
interface Participant {
    /**
     * Prepare phase: participant decides whether it can commit.
     *
     * @param transactionId identifier of the transaction
     * @return Vote.COMMIT if ready, Vote.ABORT otherwise
     * @throws RuntimeException to simulate a failure during prepare
     */
    Vote prepare(String transactionId);

    /**
     * Commit phase: coordinator instructs participant to commit.
     *
     * @param transactionId identifier of the transaction
     */
    void commit(String transactionId);

    /**
     * Abort phase: coordinator instructs participant to abort.
     *
     * @param transactionId identifier of the transaction
     */
    void abort(String transactionId);

    /**
     * Returns the current state of the participant.
     *
     * @return current ParticipantState
     */
    ParticipantState getState();
}

/**
 * Simple deterministic participant used for testing.
 * The participant will vote COMMIT if {@code willCommit} is true,
 * otherwise it votes ABORT.
 */
class SimpleParticipant implements Participant {
    private final boolean willCommit;
    private ParticipantState state = ParticipantState.INIT;

    SimpleParticipant(boolean willCommit) {
        this.willCommit = willCommit;
    }

    @Override
    public Vote prepare(String transactionId) {
        if (state != ParticipantState.INIT) {
            throw new IllegalStateException("Prepare called in invalid state: " + state);
        }
        if (willCommit) {
            state = ParticipantState.PREPARED;
            return Vote.COMMIT;
        } else {
            state = ParticipantState.ABORTED;
            return Vote.ABORT;
        }
    }

    @Override
    public void commit(String transactionId) {
        if (state != ParticipantState.PREPARED) {
            throw new IllegalStateException("Commit called in invalid state: " + state);
        }
        state = ParticipantState.COMMITTED;
    }

    @Override
    public void abort(String transactionId) {
        // Abort can be called from any non-final state.
        if (state == ParticipantState.COMMITTED) {
            throw new IllegalStateException("Abort called after commit.");
        }
        state = ParticipantState.ABORTED;
    }

    @Override
    public ParticipantState getState() {
        return state;
    }
}

/**
 * Participant that throws an exception during the prepare phase,
 * used to test coordinator's failure handling.
 */
class FaultyParticipant implements Participant {
    private ParticipantState state = ParticipantState.INIT;

    @Override
    public Vote prepare(String transactionId) {
        throw new RuntimeException("Simulated failure during prepare");
    }

    @Override
    public void commit(String transactionId) {
        // Should never be called in a correct execution.
        state = ParticipantState.COMMITTED;
    }

    @Override
    public void abort(String transactionId) {
        state = ParticipantState.ABORTED;
    }

    @Override
    public ParticipantState getState() {
        return state;
    }
}

/**
 * Coordinator implementing the Two‑Phase Commit protocol.
 * It is deterministic and does not perform any I/O.
 */
class Coordinator {
    private final List<Participant> participants;

    Coordinator(List<Participant> participants) {
        // Defensive copy to avoid external mutation.
        this.participants = new ArrayList<>(participants);
    }

    /**
     * Executes a transaction using the 2PC algorithm.
     *
     * @param transactionId identifier of the transaction
     * @return final Decision (COMMIT or ABORT)
     */
    Decision executeTransaction(String transactionId) {
        // Phase 1 – Prepare
        List<Vote> votes = new ArrayList<>(participants.size());
        for (Participant p : participants) {
            try {
                Vote v = p.prepare(transactionId);
                votes.add(v);
            } catch (RuntimeException e) {
                // Treat any failure as an ABORT vote.
                votes.add(Vote.ABORT);
            }
        }

        // Determine global decision.
        Decision decision = votes.contains(Vote.ABORT) ? Decision.ABORT : Decision.COMMIT;

        // Phase 2 – Commit or Abort
        for (Participant p : participants) {
            try {
                if (decision == Decision.COMMIT) {
                    p.commit(transactionId);
                } else {
                    p.abort(transactionId);
                }
            } catch (RuntimeException e) {
                // In a real system we would log the error; here we ignore it.
            }
        }
        return decision;
    }

    /**
     * Returns an immutable view of the participants list.
     *
     * @return list of participants
     */
    List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }
}
