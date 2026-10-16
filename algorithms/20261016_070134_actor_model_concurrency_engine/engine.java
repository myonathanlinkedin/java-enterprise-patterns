package actor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core engine that manages actors and their mailboxes.
 */
public final class ActorSystem {
    private final ConcurrentMap<ActorRef, Thread> actorThreads = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(0);

    /**
     * Registers a new actor with the system.
     *
     * @param actor the actor instance; must not be {@code null}
     * @return a reference that can be used to send messages to the actor
     */
    public ActorRef registerActor(Actor actor) {
        if (actor == null) {
            throw new IllegalArgumentException("Actor cannot be null");
        }
        String id = "Actor-" + idGenerator.incrementAndGet();
        ActorRef ref = new ActorRef(actor, id, this);
        Thread t = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Envelope env = ref.mailbox.take();
                    try {
                        actor.receive(env.msg, ref, env.sender);
                    } catch (Throwable t2) {
                        System.err.println("Exception in actor " + id + ": " + t2);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, id);
        t.start();
        actorThreads.put(ref, t);
        return ref;
    }

    /* package-private */ void enqueue(ActorRef recipient, Message msg, ActorRef sender) {
        recipient.enqueue(msg, sender);
    }

    /**
     * Shuts down the actor system, interrupting all actor threads.
     */
    public void shutdown() {
        for (Thread t : actorThreads.values()) {
            t.interrupt();
        }
        for (Thread t : actorThreads.values()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        actorThreads.clear();
    }
}
