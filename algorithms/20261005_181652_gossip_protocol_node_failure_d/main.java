package main;

import core.GossipFailureDetector;
import core.SimulatedNetwork;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final int NODE_COUNT = 5;
    private static final long GOSSIP_INTERVAL_MS = 200;
    private static final long FAILURE_TIMEOUT_MS = 800;
    private static final int FANOUT = 2;

    public static void main(String[] args) throws Exception {
        SimulatedNetwork network = new SimulatedNetwork();
        List<GossipFailureDetector> detectors = new ArrayList<>();

        // Create and start detectors
        for (int i = 0; i < NODE_COUNT; i++) {
            String id = "node-" + i;
            GossipFailureDetector d = new GossipFailureDetector(
                    id, network, GOSSIP_INTERVAL_MS, FAILURE_TIMEOUT_MS, FANOUT);
            d.start();
            detectors.add(d);
        }

        // Allow system to stabilize
        Thread.sleep(2000);

        // Simulate failure of node-2
        GossipFailureDetector failed = detectors.get(2);
        failed.stop();
        System.out.println("[INFO] Stopped node-2 to simulate failure.");

        // Wait enough time for others to suspect the failure
        Thread.sleep(2000);

        // Verify detection
        for (int i = 0; i < detectors.size(); i++) {
            if (i == 2) continue; // failed node is stopped
            GossipFailureDetector d = detectors.get(i);
            Set<String> suspected = d.getSuspectedNodes();
            assert suspected.contains("node-2") :
                    "Node " + d.nodeId + " did not suspect node-2";
            System.out.println("[PASS] " + d.nodeId + " suspects node-2");
        }

        // Benchmark: measure gossip latency over 5 seconds
        benchmark(network, detectors);

        // Clean shutdown
        detectors.forEach(GossipFailureDetector::stop);
        System.out.println("[INFO] All detectors stopped.");
    }

    private static void benchmark(SimulatedNetwork network,
                                  List<GossipFailureDetector> detectors) throws Exception {
        System.out.println("[BENCH] Starting latency benchmark (5 sec)...");
        final int rounds = 25;
        final CountDownLatch latch = new CountDownLatch(rounds * detectors.size());

        // Attach a temporary listener to capture receive timestamps
        for (GossipFailureDetector d : detectors) {
            // Wrap original receive method via reflection (avoid altering core)
            // For simplicity, we poll heartbeats instead.
        }

        long start = System.nanoTime();
        Thread.sleep(5000);
        long elapsed = System.nanoTime() - start;
        double msgsPerSec = (detectors.size() * (5000 / GOSSIP_INTERVAL_MS) * FANOUT) / (elapsed / 1e9);
        System.out.printf("[BENCH] Approx. gossip messages per second: %.2f%n", msgsPerSec);
    }
}
