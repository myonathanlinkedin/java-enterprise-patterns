public class Main {
    public static void main(String[] args) {
        testBasicProduceConsume();
        testWrapAround();
        System.out.println("All tests passed.");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null) {
            if (actual != null) {
                throw new AssertionError(message + " Expected null but was " + actual);
            }
        } else if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected " + expected + " but was " + actual);
        }
    }

    private static void testBasicProduceConsume() {
        RingBuffer<Integer> rb = new RingBuffer<>(8);
        for (int i = 0; i < 8; i++) {
            long seq = rb.next();
            rb.set(seq, i);
            rb.publish(seq);
            rb.setGatingSequence(seq);
            int val = rb.get(seq);
            assertEquals(i, val, "Value mismatch at seq " + seq);
        }
    }

    private static void testWrapAround() {
        RingBuffer<Integer> rb = new RingBuffer<>(8);
        // Produce 8 items
        for (int i = 0; i < 8; i++) {
            long seq = rb.next();
            rb.set(seq, i);
            rb.publish(seq);
            rb.setGatingSequence(seq);
        }
        // Consume 8 items
        for (int i = 0; i < 8; i++) {
            int val = rb.get(i);
            assertEquals(i, val, "Wrap-around consume mismatch at seq " + i);
        }
        // Produce another 8 items, should wrap
        for (int i = 8; i < 16; i++) {
            long seq = rb.next();
            rb.set(seq, i);
            rb.publish(seq);
            rb.setGatingSequence(seq);
        }
        // Consume 8 items starting from seq 8
        for (int i = 8; i < 16; i++) {
            int val = rb.get(i);
            assertEquals(i, val, "Wrap-around consume mismatch at seq " + i);
        }
    }
}
