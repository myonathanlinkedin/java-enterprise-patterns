import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        singleThreadTests();
        multiThreadStressTest();
        System.out.println("All tests passed.");
    }

    private static void singleThreadTests() {
        OptimisticList<Integer> list = new OptimisticList<>();
        assert list.add(5);
        assert list.add(3);
        assert list.add(7);
        assert !list.add(5); // duplicate
        assert list.contains(3);
        assert list.contains(5);
        assert list.contains(7);
        assert !list.contains(4);
        assert list.remove(5);
        assert !list.contains(5);
        assert !list.remove(5); // already removed
        List<Integer> expected = Arrays.asList(3, 7);
        assert list.toList().equals(expected) : "Expected " + expected + " got " + list.toList();
    }

    private static void multiThreadStressTest() throws Exception {
        final OptimisticList<Integer> list = new OptimisticList<>();
        final ConcurrentSkipListSet<Integer> reference = new ConcurrentSkipListSet<>();
        final int threadCount = 8;
        final int opsPerThread = 2000;
        ExecutorService exec = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger adds = new AtomicInteger();
        AtomicInteger removes = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            exec.submit(() -> {
                try {
                    Random rand = new Random();
                    startLatch.await();
                    for (int j = 0; j < opsPerThread; j++) {
                        int val = rand.nextInt(100);
                        int op = rand.nextInt(3);
                        switch (op) {
                            case 0: // add
                                if (list.add(val)) {
                                    reference.add(val);
                                    adds.incrementAndGet();
                                }
                                break;
                            case 1: // remove
                                if (list.remove(val)) {
                                    reference.remove(val);
                                    removes.incrementAndGet();
                                }
                                break;
                            case 2: // contains
                                boolean a = list.contains(val);
                                boolean b = reference.contains(val);
                                assert a == b : "Contain mismatch for " + val;
                                break;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // start all threads
        doneLatch.await();
        exec.shutdownNow();

        // Verify final state matches reference set
        List<Integer> listContents = list.toList();
        List<Integer> refContents = new ArrayList<>(reference);
        assert listContents.equals(refContents) : "Final list mismatch. Expected " + refContents + " got " + listContents;
        System.out.println("Stress test completed: adds=" + adds.get() + " removes=" + removes.get());
    }
}
