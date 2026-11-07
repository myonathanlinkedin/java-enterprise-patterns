import java.util.*;

class RaftEngine {
    private final Map<Integer, Node> nodes = new HashMap<>();

    void addNode(Node node) {
        nodes.put(node.id, node);
    }

    int getMajority() {
        return nodes.size() / 2 + 1;
    }

    void startElection(int nodeId) {
        Node candidate = nodes.get(nodeId);
        if (candidate == null) {
            throw new IllegalArgumentException("Node not found: " + nodeId);
        }
        candidate.state = NodeState.CANDIDATE;
        candidate.currentTerm++;
        candidate.votedFor = candidate.id;
        candidate.votesReceived = 1; // vote for self

        for (Node peer : nodes.values()) {
            if (peer.id == candidate.id) {
                continue;
            }
            RequestVote rv = new RequestVote(candidate.currentTerm, candidate.id);
            boolean granted = peer.receiveRequestVote(rv);
            candidate.receiveVoteResponse(new VoteResponse(rv.term, granted));
        }

        if (candidate.state == NodeState.CANDIDATE &&
            candidate.votesReceived >= getMajority()) {
            candidate.state = NodeState.LEADER;
        } else if (candidate.state == NodeState.CANDIDATE) {
            candidate.state = NodeState.FOLLOWER;
            candidate.votedFor = null;
            candidate.votesReceived = 0;
        }
    }

    Node getNode(int id) {
        return nodes.get(id);
    }
}
