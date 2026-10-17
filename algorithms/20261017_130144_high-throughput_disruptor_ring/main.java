import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {
        testSingleProducerSingleConsumer();
        testMultipleProducers();
        testWrapAround();
        System.out.println("All tests passed.");
    }

    private static void testSingleProducerSingleConsumer() throws Exception {
        RingBuffer<Integer> rb = new RingBuffer<>(8);
        EventProcessor<Integer> processor = new EventProcessor<>(rb);
        Thread consumer = new Thread(processor);
        consumer.start();

        for (int i = 0; i < 1000; i++) {
            long seq = rb.next();
            rb.publish(seq, i);
        }

        Thread.sleep(100);
        processor.stop();
        consumer.join();

        if (rb.getCursor() != 999) {
            throw new AssertionError("Cursor mismatch");
        }
        if (rb.getGatingSequence() != 999) {
            throw new AssertionError("Gating sequence mismatch");
        }
    }

    private static void testMultipleProducers() throws Exception {
        RingBuffer<Integer> rb = new RingBuffer<>(16);
        EventProcessor<Integer> processor = new EventProcessor<>(rb);
        Thread consumer = new Thread(processor);
        consumer.start();

        int producerCount = 4;
        ExecutorService exec = Executors.newFixedThreadPool(producerCount);
        for (int p = 0; p < producerCount; p++) {
            final int base = p * 250;
            exec.submit(() -> {
                for (int i = 0; i < 250; i++) {
                    long seq = rb.next();
                    rb.publish(seq, base + i);
                }
            });
        }
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        Thread.sleep(200);
        processor.stop();
        consumer.join();

        if (rb.getCursor() != 999) {
            throw new AssertionError("Cursor mismatch in multi-producer");
        }
        if (rb.getGatingSequence() != 999) {
            throw new AssertionError("Gating sequence mismatch in multi-producer");
        }
    }

    private static void testWrapAround() throws Exception {
        RingBuffer<Integer> rb = new RingBuffer<>(8);
        EventProcessor<Integer> processor = new EventProcessor<>(rb);
        Thread consumer = new Thread(processor);
        consumer.start();

        for (int i = 0; i < 20; i++) {
            long seq = rb.next();
            rb.publish(seq, i);
        }

        Thread.sleep(200);
        processor.stop();
        consumer.join();

        if (rb.getCursor() != 19) {
            throw new AssertionError("Cursor mismatch after wrap");
        }
        if (rb.getGatingSequence() != 19) {
            throw new AssertionError("Gating sequence mismatch after wrap");
        }
    }
}
