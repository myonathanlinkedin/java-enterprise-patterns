import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Minimal Actor Model implementation.
 * Actors process messages sequentially from a mailbox using a shared thread pool.
 */
public final class ActorSystem {
    private final ExecutorService executor;

    public ActorSystem() {
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Creates a new actor with the given behavior.
     *
     * @param behavior function that processes a single message
     * @param <T>      type of messages
     * @return reference to the created actor
     */
    public <T> ActorRef<T> actorOf(Consumer<T> behavior) {
        Mailbox<T> mailbox = new Mailbox<>(behavior);
        ActorRef<T> ref = new ActorRef<>(mailbox);
        executor.submit(mailbox);
        return ref;
    }

    /**
     * Gracefully shuts down the underlying thread pool.
     * Waits up to 5 seconds for termination.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    // --- Inner classes -------------------------------------------------------

    /**
     * Reference to an actor; used by clients to send messages.
     *
     * @param <T> message type
     */
    public static final class ActorRef<T> {
        private final Mailbox<T> mailbox;

        private ActorRef(Mailbox<T> mailbox) {
            this.mailbox = mailbox;
        }

        /**
         * Sends a message to the actor's mailbox.
         *
         * @param msg message to deliver
         * @throws IllegalStateException if the actor has been stopped
         */
        public void tell(T msg) {
            mailbox.enqueue(msg);
        }

        /**
         * Stops the actor; pending messages are still processed.
         */
        public void stop() {
            mailbox.stop();
        }
    }

    /**
     * Mailbox that queues messages and processes them sequentially.
     *
     * @param <T> message type
     */
    private static final class Mailbox<T> implements Runnable {
        private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();
        private final Consumer<T> behavior;
        private volatile boolean active = true;

        Mailbox(Consumer<T> behavior) {
            this.behavior = behavior;
        }

        void enqueue(T msg) {
            if (!active) {
                throw new IllegalStateException("Actor has been stopped");
            }
            queue.offer(msg);
        }

        void stop() {
            active = false;
        }

        @Override
        public void run() {
            try {
                while (active || !queue.isEmpty()) {
                    T msg = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        try {
                            behavior.accept(msg);
                        } catch (Throwable t) {
                            // Swallow exceptions to keep mailbox alive; in production,
                            // replace with proper supervision.
                            t.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
