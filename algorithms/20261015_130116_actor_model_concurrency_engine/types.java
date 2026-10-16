import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple immutable message wrapper.
 */
public final class Message {
    private final Object payload;

    public Message(Object payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Message payload cannot be null");
        }
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{" + "payload=" + payload + '}';
    }
}

/**
 * Actor contract. Implementations must handle incoming messages.
 */
public interface Actor {
    /**
     * Called by the engine for each message taken from the actor's mailbox.
     *
     * @param msg the message to process
     */
    void receive(Message msg);
}

/**
 * Thread‑safe mailbox based on a blocking queue.
 */
public final class Mailbox {
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

    /**
     * Enqueue a message. Called by {@link ActorRef}.
     *
     * @param msg the message to enqueue
     */
    public void enqueue(Message msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Cannot enqueue null message");
        }
        queue.offer(msg);
    }

    /**
     * Retrieve and remove the head of the queue, waiting if necessary.
     *
     * @return the next message
     * @throws InterruptedException if interrupted while waiting
     */
    public Message take() throws InterruptedException {
        return queue.take();
    }

    /**
     * Non‑blocking poll used for graceful shutdown.
     *
     * @return the next message or {@code null} if none available
     */
    public Message poll() {
        return queue.poll();
    }
}

/**
 * Reference to an actor that can be used by other actors or external code to send messages.
 */
public final class ActorRef {
    private final Mailbox mailbox;

    public ActorRef(Mailbox mailbox) {
        if (mailbox == null) {
            throw new IllegalArgumentException("Mailbox cannot be null");
        }
        this.mailbox = mailbox;
    }

    /**
     * Send a message to the associated actor.
     *
     * @param msg the message to send
     */
    public void send(Message msg) {
        mailbox.enqueue(msg);
    }

    /**
     * Accessor for testing / introspection.
     *
     * @return the underlying mailbox
     */
    Mailbox getMailbox() {
        return mailbox;
    }
}
