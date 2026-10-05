//=== File: main.java ===
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EdgeAgent orchestrates on-device LLM inference for multi-agent systems.
 * This single-file implementation includes core components and basic unit tests.
 */
public class Main {

    // ---------- Core Components ----------
    /** Simple mock LLM model performing deterministic transformations. */
    static class LLMModel {
        private final String modelName;

        LLMModel(String modelName) {
            this.modelName = Objects.requireNonNull(modelName);
        }

        /** Mock inference: reverse the prompt and append model name. */
        String infer(String prompt) {
            Objects.requireNonNull(prompt);
            StringBuilder sb = new StringBuilder(prompt).reverse();
            sb.append(" [").append(modelName).append("]");
            // Simulate computation latency
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            return sb.toString();
        }
    }

    /** Represents a unit of work for an agent. */
    static class Task {
        private static final AtomicInteger ID_GEN = new AtomicInteger(0);
        final int id;
        final String prompt;
        volatile String result; // set after processing

        Task(String prompt) {
            this.id = ID_GEN.incrementAndGet();
            this.prompt = Objects.requireNonNull(prompt);
        }
    }

    /** Agent capable of processing tasks using its LLM model. */
    static class Agent implements Callable<Void> {
        private final String name;
        private final LLMModel model;
        private final BlockingQueue<Task> queue;
        private volatile boolean running = true;

        Agent(String name, LLMModel model, BlockingQueue<Task> queue) {
            this.name = Objects.requireNonNull(name);
            this.model = Objects.requireNonNull(model);
            this.queue = Objects.requireNonNull(queue);
        }

        /** Graceful shutdown signal. */
        void stop() { running = false; }

        @Override
        public Void call() {
            while (running || !queue.isEmpty()) {
                try {
                    Task task = queue.poll(50, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        task.result = model.infer(task.prompt);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }
    }

    /** Scheduler distributes tasks among agents using a thread pool. */
    static class Scheduler {
        private final List<Agent> agents;
        private final ExecutorService executor;
        private final BlockingQueue<Task> taskQueue;

        Scheduler(List<LLMModel> models) {
            this.taskQueue = new LinkedBlockingQueue<>();
            this.agents = new ArrayList<>();
            for (int i = 0; i < models.size(); i++) {
                agents.add(new Agent("Agent-" + (i + 1), models.get(i), taskQueue));
            }
            this.executor = Executors.newFixedThreadPool(agents.size());
            agents.forEach(agent -> executor.submit(agent));
        }

        /** Submit a new task for processing. */
        Task submit(String prompt) {
            Task task = new Task(prompt);
            taskQueue.offer(task);
            return task;
        }

        /** Await completion of all submitted tasks. */
        void awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
            long deadline = System.nanoTime() + unit.toNanos(timeout);
            while (System.nanoTime() < deadline) {
                if (taskQueue.isEmpty() && agents.stream().allMatch(a -> a.queue.isEmpty())) {
                    return;
                }
                Thread.sleep(20);
            }
            throw new TimeoutException("Tasks did not complete in time");
        }

        /** Shut down all agents and the executor. */
        void shutdown() {
            agents.forEach(Agent::stop);
            executor.shutdownNow();
        }
    }

    // ---------- Unit Tests ----------
    static void runTests() throws Exception {
        // Test LLMModel inference
        LLMModel model = new LLMModel("TestModel");
        String out = model.infer("hello");
        assert out.equals("olleh [TestModel]") : "LLMModel inference failed";

        // Test Agent processing a single task
        BlockingQueue<Task> q = new LinkedBlockingQueue<>();
        Agent agent = new Agent("TestAgent", model, q);
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<Void> f = exec.submit(agent);
        Task t = new Task("world");
        q.offer(t);
        Thread.sleep(50);
        assert t.result != null && t.result.equals("dlrow [TestModel]") : "Agent processing failed";
        agent.stop();
        exec.shutdownNow();

        // Test Scheduler with multiple agents
        List<LLMModel> models = Arrays.asList(
                new LLMModel("M1"),
                new LLMModel("M2")
        );
        Scheduler scheduler = new Scheduler(models);
        Task t1 = scheduler.submit("alpha");
        Task t2 = scheduler.submit("beta");
        scheduler.awaitCompletion(2, TimeUnit.SECONDS);
        assert t1.result.equals("ahpla [M1]") || t1.result.equals("ahpla [M2]") : "Scheduler result mismatch";
        assert t2.result.equals("ateb [M1]") || t2.result.equals("ateb [M2]") : "Scheduler result mismatch";
        scheduler.shutdown();
    }

    // ---------- Driver ----------
    public static void main(String[] args) throws Exception {
        // Enable assertions if not already enabled
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        runTests();
        System.out.println("All tests passed.");
    }
}
//=== End of main.java ===
