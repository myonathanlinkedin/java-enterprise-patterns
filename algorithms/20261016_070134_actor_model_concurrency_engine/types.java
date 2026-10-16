package actor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Marker interface for messages exchanged between actors.
 */
public interface Message { }

/**
 * Interface that all actors must implement.
 */
public interface Actor {
    /**
     * Called when a message is delivered to the actor.
     *
     * @param msg   the message payload
     * @param self  reference to the actor itself
     * @param sender reference to the actor that sent the message; may be {@code null}
     */
    void receive(Message msg, ActorRef self, ActorRef sender);
}

/**
 * Internal wrapper that couples a message with its sender.
 */
final class Envelope {
    final Message msg;
    final ActorRef sender;

    Envelope(Message msg, ActorRef sender) {
        this.msg = msg;
        this.sender = sender;
    }
}

/**
 * Handle used by actors to send messages to each other.
 */
public final class ActorRef {
    final BlockingQueue<Envelope> mailbox;
    final Actor actor;
    final String id;
    final ActorSystem system;

    ActorRef(Actor actor, String id, ActorSystem system) {
        this.actor = actor;
        this.id = id;
        this.system = system;
        this.mailbox = new LinkedBlockingQueue<>();
    }

    /**
     * Sends a message to this actor.
     *
     * @param msg   the message payload; must not be {@code null}
     * @param sender reference to the sending actor; may be {@code null}
     */
    public void send(Message msg, ActorRef sender) {
        if (msg == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        system.enqueue(this, msg, sender);
    }

    /* package-private */ void enqueue(Message msg, ActorRef sender) {
        mailbox.offer(new Envelope(msg, sender));
    }
}
