import java.util.*;

public final class Node {
    private final int id;
    private final Set<Integer> neighbors;
    private final Map<Integer, Integer> suspicion;
    private boolean alive;
    private final int threshold;

    public Node(int id, Set<Integer> neighbors, int threshold) {
        this.id = id;
        this.neighbors = new HashSet<>(neighbors);
        this.threshold = threshold;
        this.suspicion = new HashMap<>();
        this.alive = true;
        for (int n : neighbors) {
            suspicion.put(n, 0);
        }
    }

    public int getId() { return id; }

    public boolean isAlive() { return alive; }

    public void setAlive(boolean alive) { this.alive = alive; }

    public Set<Integer> getNeighbors() { return Collections.unmodifiableSet(neighbors); }

    public Map<Integer, Integer> getSuspicion() { return suspicion; }

    public int getThreshold() { return threshold; }

    public void resetSuspicion(int neighborId) { suspicion.put(neighborId, 0); }

    public void incrementSuspicion(int neighborId) {
        suspicion.put(neighborId, suspicion.getOrDefault(neighborId, 0) + 1);
    }

    public boolean isSuspectedFailed(int neighborId) {
        return suspicion.getOrDefault(neighborId, 0) > threshold;
    }

    public Set<Integer> getSuspectedFailedNodes() {
        Set<Integer> suspected = new HashSet<>();
        for (Map.Entry<Integer, Integer> e : suspicion.entrySet()) {
            if (e.getValue() > threshold) {
                suspected.add(e.getKey());
            }
        }
        return suspected;
    }
}
