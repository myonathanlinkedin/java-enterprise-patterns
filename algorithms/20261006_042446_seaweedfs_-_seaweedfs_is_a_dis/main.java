import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Running tests...");
        testConsistentHashRing();
        testMetadataStore();
        testSeaweedFS();
        benchmarkSeaweedFS();
        System.out.println("All tests completed.");
    }

    private static void testConsistentHashRing() {
        ConsistentHashRing ring = new ConsistentHashRing(10);
        ring.addNode("NodeA");
        ring.addNode("NodeB");
        ring.addNode("NodeC");

        Map<String, Integer> distribution = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            String key = "key" + i;
            String node = ring.getNode(key);
            distribution.put(node, distribution.getOrDefault(node, 0) + 1);
        }

        assert distribution.size() == 3 : "Expected 3 nodes in distribution";
        for (Map.Entry<String, Integer> e : distribution.entrySet()) {
            System.out.println("Node " + e.getKey() + " has " + e.getValue() + " keys");
        }
    }

    private static void testMetadataStore() {
        MetadataStore store = new MetadataStore();
        FileMetadata meta = new FileMetadata("/file1", 1024, Arrays.asList("NodeA", "NodeB"));
        store.addFile(meta);

        FileMetadata retrieved = store.getFile("/file1");
        assert retrieved != null : "File should exist";
        assert retrieved.getSize() == 1024 : "Size mismatch";
        assert retrieved.getReplicas().size() == 2 : "Replica count mismatch";

        store.deleteFile("/file1");
        assert store.getFile("/file1") == null : "File should be deleted";
    }

    private static void testSeaweedFS() {
        List<String> nodes = Arrays.asList("Node1", "Node2", "Node3");
        SeaweedFS fs = new SeaweedFS(nodes, 20, 2);

        fs.createFile("/docs/report.pdf", 2048);
        FileMetadata meta = fs.readFile("/docs/report.pdf");
        assert meta != null : "File should exist";
        assert meta.getReplicas().size() == 2 : "Should have 2 replicas";

        fs.deleteFile("/docs/report.pdf");
        assert fs.readFile("/docs/report.pdf") == null : "File should be deleted";
    }

    private static void benchmarkSeaweedFS() {
        List<String> nodes = Arrays.asList("NodeA", "NodeB", "NodeC", "NodeD");
        SeaweedFS fs = new SeaweedFS(nodes, 30, 3);
        int count = 10000;
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            fs.createFile("/file" + i, 512);
        }
        long duration = System.nanoTime() - start;
        System.out.printf("Created %d files in %.3f ms%n", count, duration / 1_000_000.0);
    }
}
