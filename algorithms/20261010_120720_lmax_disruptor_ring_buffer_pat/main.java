import java.util.*;

public class Main {
    private static void assertEqual(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(msg + " Expected: " + expected + " but was: " + actual);
        }
    }

    private static void testSingleProducerSingleConsumer() {
        RingBuffer<String> ring = new RingBuffer<>(8, new Sequence());
        Consumer consumer = new Consumer(ring);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        Producer producer = new Producer(ring);
        producer.publish("1");
        producer.publish("2");
        producer.publish("3");
        producer.publish("END");

        try { consumerThread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        List<String> processed = consumer.getProcessed();
        assertEqual(4, processed.size(), "Processed count mismatch");
        assertEqual(Arrays.asList("1","2","3","END"), processed, "Event order mismatch");
    }

    private static void testWrapAround() {
        RingBuffer<String> ring = new RingBuffer<>(8, new Sequence());
        Consumer consumer = new Consumer(ring);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        Producer producer = new Producer(ring);
        for (int i = 0; i < 10; i++) {
            producer.publish("msg" + i);
        }
        producer.publish("END");

        try { consumerThread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        List<String> processed = consumer.getProcessed();
        assertEqual(11, processed.size(), "Wrap-around processed count mismatch");
        for (int i = 0; i < 10; i++) {
            assertEqual("msg" + i, processed.get(i), "Wrap-around event mismatch at index " + i);
        }
        assertEqual("END", processed.get(10), "Wrap-around END event mismatch");
    }

    public static void main(String[] args) {
        testSingleProducerSingleConsumer();
        testWrapAround();
        System.out.println("All tests passed.");
    }
}
