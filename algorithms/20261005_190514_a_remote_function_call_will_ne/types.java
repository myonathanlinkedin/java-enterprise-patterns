import java.util.concurrent.*;
import java.util.*;

public interface RemoteFunction<T, R> {
    R apply(T input) throws Exception;
}

public class RemoteCall<T, R> {
    private final RemoteFunction<T, R> function;
    private final T input;

    public RemoteCall(RemoteFunction<T, R> function, T input) {
        this.function = function;
        this.input = input;
    }

    public RemoteFunction<T, R> getFunction() {
        return function;
    }

    public T getInput() {
        return input;
    }
}

public class RemoteResult<R> {
    private final R result;
    private final boolean success;
    private final Exception exception;

    public RemoteResult(R result) {
        this.result = result;
        this.success = true;
        this.exception = null;
    }

    public RemoteResult(Exception exception) {
        this.result = null;
        this.success = false;
        this.exception = exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public R getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}

public class RemoteExecutor {
    private final ExecutorService executor;
    private final long latencyMillis;

    public RemoteExecutor(int threads, long latencyMillis) {
        this.executor = Executors.newFixedThreadPool(threads);
        this.latencyMillis = latencyMillis;
    }

    public <T, R> Future<RemoteResult<R>> submit(RemoteCall<T, R> call) {
        return executor.submit(() -> {
            try {
                Thread.sleep(latencyMillis);
                R res = call.getFunction().apply(call.getInput());
                return new RemoteResult<>(res);
            } catch (Exception e) {
                return new RemoteResult<>(e);
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
