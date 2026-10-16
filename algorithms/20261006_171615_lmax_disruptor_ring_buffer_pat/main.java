import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) throws Exception {
        testSingleThread();
        testMultiThread();
        testWrapAround();
        System.out.println("All tests passed.");
    }

    private static void testSingleThread() {
        RingBuffer<Integer> rb = new RingBuffer<>(8);
        for (int i = 0; i < 10; i++) {
            long seq = rb.next();
            rb.publish(seq, i);
        }
        List<Integer> result = new ArrayList<>();
        while (rb.hasNext()) {
            result.add(rb.nextValue());
        }
        assert result.size() == 10 : "Expected 10 items, got " + result.size();
        for (int i = 0; i < 10; i++) {
            assert result.get(i) == i : "Expected " + i + ", got " + result.get(i);
        }
    }

    private static void testMultiThread() throws Exception {
        final RingBuffer<Integer> rb = new RingBuffer<>(16);
        final int count = 1000;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<Integer> consumerList = new ArrayList<>();

        Thread producer = new Thread(() -> {
            try {
                start.await();
                for (int i = 0; i < count; i++) {
                    long seq = rb.next();
                    rb.publish(seq, i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                start.await();
                int read = 0;
                while (read < count) {
                    if (rb.hasNext()) {
                        consumerList.add(rb.nextValue());
                        read++;
                    } else {
                        Thread.yield();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });

        producer.start();
        consumer.start();
        start.countDown();
        done.await();

        assert consumerList.size() == count : "Expected " + count + " items, got " + consumerList.size();
        for (int i = 0; i < count; i++) {
            assert consumerList.get(i) == i : "Expected " + i + ", got " + consumerList.get(i);
        }
    }

    private static void testWrapAround() {
        RingBuffer<Integer> rb = new RingBuffer<>(4);
        for (int i = 0; i < 8; i++) {
            long seq = rb.next();
            rb.publish(seq, i);
        }
        List<Integer> result = new ArrayList<>();
        while (rb.hasNext()) {
            result.add(rb.nextValue());
        }
        assert result.size() == 8 : "Expected 8 items, got " + result.size();
        for (int i = 0; i < 8; i++) {
            assert result.get(i) == i : "Expected " + i + ", got " + result.get(i);
        }
    }
}
