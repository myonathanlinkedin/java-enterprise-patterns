import java.util.concurrent.locks.ReentrantLock;

public class PoisonableLock {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean poisoned = false;

    public void runWithLock(Runnable task) {
        lock.lock();
        try {
            task.run();
        } catch (RuntimeException e) {
            poisoned = true;
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public boolean isPoisoned() {
        return poisoned;
    }

    public void resetPoison() {
        if (!poisoned) {
            throw new IllegalStateException("Lock is not poisoned");
        }
        poisoned = false;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
