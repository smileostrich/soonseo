package org.ian.soonseo;

import java.util.concurrent.*;

public class CallableTaskWrapper<T> implements TaskWrapper {

    private final Callable<T> task;
    private final CompletableFuture<T> future;
    private final TaskQueueConfig config;

    public CallableTaskWrapper(Callable<T> task, CompletableFuture<T> future, TaskQueueConfig config) {
        this.task = task;
        this.future = future;
        this.config = config;
    }

    @Override
    public void execute() throws Exception {
        try {
            T result = task.call();

            if (config.getTaskTimeout() != null) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<T> timeoutFuture = executor.submit(task);
                result = timeoutFuture.get(config.getTaskTimeout().toMillis(), TimeUnit.MILLISECONDS);
                executor.shutdown();
            }

            future.complete(result);

        } catch (TimeoutException e) {
            future.completeExceptionally(new TimeoutException("Task execution timed out"));
            throw e;
        } catch (Exception e) {
            future.completeExceptionally(e);
            throw e;
        }
    }

}
