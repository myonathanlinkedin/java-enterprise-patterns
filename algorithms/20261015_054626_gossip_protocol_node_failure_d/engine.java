import java.util.*;

public final class GossipEngine {
    private final Map<Integer, Node> nodes;
    private final Random random;
    private final int rounds;

    public GossipEngine(Map<Integer, Node> nodes, int rounds, long seed) {
        this.nodes = new HashMap<>(nodes);
        this.random = new Random(seed);
        this.rounds = rounds;
    }

    public void run() {
        for (int r = 0; r < rounds; r++) {
            Map<Integer, List<Integer>> receivedFrom = new HashMap<>();
            for (Node node : nodes.values()) {
                if (!node.isAlive()) continue;
                List<Integer> neighborList = new ArrayList<>(node.getNeighbors());
                if (neighborList.isEmpty()) continue;
                int targetId = neighborList.get(random.nextInt(neighborList.size()));
                receivedFrom.computeIfAbsent(targetId, k -> new ArrayList<>()).add(node.getId());
            }
            for (Node node : nodes.values()) {
                if (!node.isAlive()) continue;
                List<Integer> senders = receivedFrom.getOrDefault(node.getId(), Collections.emptyList());
                Set<Integer> sendersSet = new HashSet<>(senders);
                for (int senderId : senders) {
                    node.resetSuspicion(senderId);
                }
                for (int neighborId : node.getNeighbors()) {
                    if (!sendersSet.contains(neighborId)) {
                        node.incrementSuspicion(neighborId);
                    }
                }
            }
        }
    }

    public Node getNode(int id) {
        return nodes.get(id);
    }

    public Collection<Node> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }
}
