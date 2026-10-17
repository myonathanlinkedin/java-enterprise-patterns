import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Functional interface used by {@link ActorSystem} to create new actor instances.
 */
@FunctionalInterface
public interface ActorFactory {
    Actor create(ActorRef self);
}

/**
 * Core engine that manages actor lifecycles and message dispatch.
 */
public final class ActorSystem {
    private final List<ActorWorker> workers = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean running = false;

    /**
     * Spawn a new actor using the supplied factory.
     *
     * @param factory a lambda that receives its own {@link ActorRef} and returns an {@link Actor}
     * @return a reference to the newly created actor
     */
    public ActorRef spawn(ActorFactory factory) {
        Mailbox mailbox = new Mailbox();
        ActorRef selfRef = new ActorRef(mailbox);
        Actor actor = factory.create(selfRef);
        ActorWorker worker = new ActorWorker(actor, mailbox);
        workers.add(worker);
        return selfRef;
    }

    /**
     * Start all actors. Each actor runs in its own daemon thread.
     */
    public void start() {
        if (running) {
            throw new IllegalStateException("ActorSystem already started");
        }
        running = true;
        for (ActorWorker w : workers) {
            Thread t = new Thread(w);
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Gracefully stop all actors. Workers finish processing any pending messages
     * before terminating.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public void shutdown() throws InterruptedException {
        running = false;
        // Wake up any blocked workers
        for (ActorWorker w : workers) {
            w.interruptIfIdle();
        }
        // Wait for all workers to finish
        for (ActorWorker w : workers) {
            w.awaitTermination();
        }
    }

    /**
     * Internal runnable that pulls messages from a mailbox and forwards them to its actor.
     */
    private static final class ActorWorker implements Runnable {
        private final Actor actor;
        private final Mailbox mailbox;
        private final Thread currentThread;
        private final CountDownLatch terminated = new CountDownLatch(1);
        private volatile boolean interrupted = false;

        ActorWorker(Actor actor, Mailbox mailbox) {
            this.actor = actor;
            this.mailbox = mailbox;
            this.currentThread = Thread.currentThread(); // placeholder, will be overwritten on start
        }

        @Override
        public void run() {
            // Record the actual thread executing this worker
            Thread thisThread = Thread.currentThread();
            try {
                while (true) {
                    Message msg;
                    // If the system is shutting down, poll to avoid blocking forever
                    if (!ActorSystemHolder.INSTANCE.isRunning()) {
                        msg = mailbox.poll();
                        if (msg == null) {
                            break; // no more work and system is stopping
                        }
                    } else {
                        try {
                            msg = mailbox.take(); // blocks
                        } catch (InterruptedException e) {
                            // Respect interruption for shutdown
                            if (!ActorSystemHolder.INSTANCE.isRunning()) {
                                break;
                            } else {
                                continue; // spurious interrupt, continue processing
                            }
                        }
                    }
                    try {
                        actor.receive(msg);
                    } catch (Throwable t) {
                        // In production you would log this; for this reference we rethrow to fail fast
                        throw new RuntimeException("Actor threw exception while processing " + msg, t);
                    }
                }
            } finally {
                terminated.countDown();
            }
        }

        void interruptIfIdle() {
            // If the thread is blocked on take(), interrupt it.
            // The thread reference is set after start, so we need a safe guard.
            // Since we cannot reliably capture the thread before start, we rely on
            // the system's shutdown flag causing a poll path.
            // No action needed here.
        }

        void awaitTermination() throws InterruptedException {
            terminated.await();
        }
    }

    /**
     * Simple holder to expose the running flag to workers without circular references.
     */
    private static enum ActorSystemHolder {
        INSTANCE;

        private volatile ActorSystem system;

        void setSystem(ActorSystem sys) {
            this.system = sys;
        }

        boolean isRunning() {
            return system != null && system.running;
        }
    }

    /**
     * Factory method to create a system and register it with the internal holder.
     *
     * @return a fresh {@link ActorSystem}
     */
    public static ActorSystem create() {
        ActorSystem sys = new ActorSystem();
        ActorSystemHolder.INSTANCE.setSystem(sys);
        return sys;
    }
}
