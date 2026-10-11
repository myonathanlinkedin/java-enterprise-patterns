package core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulated in‑memory network for gossip messages.
 */
public class SimulatedNetwork {
    private final Map<String, GossipFailureDetector> nodes = new ConcurrentHashMap<>();

    public void register(String nodeId, GossipFailureDetector detector) {
        nodes.put(nodeId, detector);
    }

    public void unregister(String nodeId) {
        nodes.remove(nodeId);
    }

    public void send(String targetId, GossipMessage msg) {
        GossipFailureDetector target = nodes.get(targetId);
        if (target != null) {
            target.receive(msg);
        }
    }

    public Set<String> allNodeIds() {
        return Collections.unmodifiableSet(nodes.keySet());
    }
}

/**
 * Simple gossip payload.
 */
class GossipMessage {
    final String senderId;
    final long heartbeat;

    GossipMessage(String senderId, long heartbeat) {
        this.senderId = senderId;
        this.heartbeat = heartbeat;
    }
}

/**
 * Failure detector based on periodic gossip of heart‑beats.
 */
public class GossipFailureDetector {
    private final String nodeId;
    private final SimulatedNetwork network;
    private final long gossipIntervalMs;
    private final long failureTimeoutMs;
    private final int fanout; // number of peers to gossip each round

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "GossipDetector-" + UUID.randomUUID());
        t.setDaemon(true);
        return t;
    });

    private final Random rand = new Random();
    private final AtomicLong heartbeat = new AtomicLong(0);

    // peerId -> last observed heartbeat timestamp (ms)
    private final ConcurrentMap<String, Long> peerHeartbeats = new ConcurrentHashMap<>();
    // peerId -> suspected flag
    private final ConcurrentMap<String, Boolean> suspected = new ConcurrentHashMap<>();

    public GossipFailureDetector(String nodeId,
                                 SimulatedNetwork network,
                                 long gossipIntervalMs,
                                 long failureTimeoutMs,
                                 int fanout) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.network = Objects.requireNonNull(network);
        this.gossipIntervalMs = gossipIntervalMs;
        this.failureTimeoutMs = failureTimeoutMs;
        this.fanout = Math.max(1, fanout);
    }

    public void start() {
        network.register(nodeId, this);
        scheduler.scheduleAtFixedRate(this::gossipAndDetect,
                gossipIntervalMs, gossipIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
        network.unregister(nodeId);
    }

    private void gossipAndDetect() {
        heartbeat.incrementAndGet();
        List<String> peers = new ArrayList<>(network.allNodeIds());
        peers.remove(nodeId);
        Collections.shuffle(peers, rand);
        int targets = Math.min(fanout, peers.size());
        for (int i = 0; i < targets; i++) {
            String target = peers.get(i);
            network.send(target, new GossipMessage(nodeId, heartbeat.get()));
        }
        detectFailures();
    }

    /** Called by the network when a gossip arrives. */
    void receive(GossipMessage msg) {
        if (msg.senderId.equals(nodeId)) return; // ignore self‑messages
        peerHeartbeats.put(msg.senderId, System.currentTimeMillis());
        suspected.put(msg.senderId, false);
    }

    private void detectFailures() {
        long now = System.currentTimeMillis();
        for (String peer : peerHeartbeats.keySet()) {
            long last = peerHeartbeats.getOrDefault(peer, 0L);
            if (now - last > failureTimeoutMs) {
                suspected.put(peer, true);
            }
        }
    }

    /** Returns an immutable snapshot of suspected failed nodes. */
    public Set<String> getSuspectedNodes() {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, Boolean> e : suspected.entrySet()) {
            if (e.getValue()) result.add(e.getKey());
        }
        return Collections.unmodifiableSet(result);
    }

    /** For testing: expose current heartbeat value. */
    long currentHeartbeat() {
        return heartbeat.get();
    }
}
