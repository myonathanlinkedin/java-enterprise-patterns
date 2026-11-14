import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;

/* Marker interface for messages */
interface Message {}

/* Envelope to carry a message together with its sender */
final class Envelope {
    final Actor sender;
    final Message msg;
    Envelope(Actor sender, Message msg) {
        this.sender = sender;
        this.msg = msg;
    }
}

/* Abstract Actor with its own mailbox and processing thread */
abstract class Actor implements Runnable {
    private final BlockingQueue<Envelope> mailbox = new LinkedBlockingQueue<>();
    private final Thread thread;
    private final AtomicBoolean running = new AtomicBoolean(true);

    protected Actor() {
        thread = new Thread(this, this.getClass().getSimpleName() + "-Thread");
        thread.start();
    }

    /* Enqueue a message from a sender */
    final void tell(Actor sender, Message msg) {
        mailbox.offer(new Envelope(sender, msg));
    }

    /* Stop the actor gracefully */
    final void stop() {
        running.set(false);
        thread.interrupt();
    }

    /* Await termination (used by tests) */
    final void awaitTermination() throws InterruptedException {
        thread.join();
    }

    /* Core processing loop */
    @Override
    public final void run() {
        try {
            while (running.get()) {
                Envelope env = mailbox.take(); // blocks
                onReceive(env.msg, env.sender);
            }
        } catch (InterruptedException ignored) {
            // Thread interrupted during shutdown
        } finally {
            // Drain remaining messages to avoid resource leaks
            mailbox.clear();
        }
    }

    /* Subclass implements message handling */
    protected abstract void onReceive(Message msg, Actor sender);
}

/* Simple ActorSystem to manage lifecycle of actors */
final class ActorSystem {
    private final List<Actor> actors = new ArrayList<>();

    /* Factory method to create and register an actor */
    Actor createActor(Supplier<? extends Actor> supplier) {
        Actor a = supplier.get();
        synchronized (actors) {
            actors.add(a);
        }
        return a;
    }

    /* Stop all actors and wait for termination */
    void shutdown() throws InterruptedException {
        List<Actor> snapshot;
        synchronized (actors) {
            snapshot = new ArrayList<>(actors);
        }
        for (Actor a : snapshot) {
            a.stop();
        }
        for (Actor a : snapshot) {
            a.awaitTermination();
        }
    }
}

/* Concrete messages used in tests */
final class Ping implements Message {
    final int remaining;
    Ping(int remaining) { this.remaining = remaining; }
}
final class Pong implements Message {
    final int remaining;
    Pong(int remaining) { this.remaining = remaining; }
}
final class Stop implements Message {}

/* PingActor sends Ping messages to a partner PongActor */
final class PingActor extends Actor {
    private final Actor partner;
    private final java.util.concurrent.CountDownLatch latch;

    PingActor(Actor partner, java.util.concurrent.CountDownLatch latch) {
        this.partner = partner;
        this.latch = latch;
    }

    @Override
    protected void onReceive(Message msg, Actor sender) {
        if (msg instanceof Ping) {
            Ping p = (Ping) msg;
            if (p.remaining > 0) {
                partner.tell(this, new Ping(p.remaining - 1));
            } else {
                // Termination condition
                partner.tell(this, new Stop());
                this.tell(this, new Stop());
            }
        } else if (msg instanceof Stop) {
            this.stop();
        }
    }
}

/* PongActor replies with Pong messages and counts down a latch */
final class PongActor extends Actor {
    private final java.util.concurrent.CountDownLatch latch;

    PongActor(java.util.concurrent.CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    protected void onReceive(Message msg, Actor sender) {
        if (msg instanceof Ping) {
            Ping p = (Ping) msg;
            if (p.remaining >= 0) {
                sender.tell(this, new Pong(p.remaining));
                latch.countDown();
            }
        } else if (msg instanceof Stop) {
            this.stop();
        }
    }
}
