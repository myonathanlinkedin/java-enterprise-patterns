import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ActorHandler {
    void handle(Object message);
}

public class Actor {
    private final BlockingQueue<Object> mailbox = new LinkedBlockingQueue<>();
    private final ActorHandler handler;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Actor(ActorHandler handler, ExecutorService executor) {
        if (handler == null || executor == null) {
            throw new NullPointerException("Handler and executor must not be null");
        }
        this.handler = handler;
        this.executor = executor;
    }

    public void send(Object msg) {
        if (msg == null) {
            throw new IllegalArgumentException("Null messages are not allowed");
        }
        mailbox.offer(msg);
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return; // already started
        }
        executor.submit(() -> {
            try {
                while (running.get() || !mailbox.isEmpty()) {
                    Object msg = mailbox.poll(100, TimeUnit.MILLISECONDS);
                    if (msg != null) {
                        handler.handle(msg);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void stop() {
        running.set(false);
    }
}

public class ActorSystem {
    private final ExecutorService executor;

    public ActorSystem(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("Thread count must be positive");
        }
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public Actor actor(ActorHandler handler) {
        Actor a = new Actor(handler, executor);
        a.start();
        return a;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
