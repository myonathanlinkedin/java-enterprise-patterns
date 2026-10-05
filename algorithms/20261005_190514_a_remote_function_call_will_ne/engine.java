import java.util.concurrent.*;
import java.util.*;

public class RemoteEngine {
    private final RemoteExecutor executor;
    private final ConcurrentMap<String, RemoteFunction<?, ?>> registry = new ConcurrentHashMap<>();

    public RemoteEngine(int threads, long latencyMillis) {
        this.executor = new RemoteExecutor(threads, latencyMillis);
    }

    public <T, R> void register(String name, RemoteFunction<T, R> function) {
        registry.put(name, function);
    }

    @SuppressWarnings("unchecked")
    public <T, R> Future<RemoteResult<R>> call(String name, T input) {
        RemoteFunction<T, R> func = (RemoteFunction<T, R>) registry.get(name);
        if (func == null) {
            throw new IllegalArgumentException("Function not registered: " + name);
        }
        RemoteCall<T, R> call = new RemoteCall<>(func, input);
        return executor.submit(call);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
