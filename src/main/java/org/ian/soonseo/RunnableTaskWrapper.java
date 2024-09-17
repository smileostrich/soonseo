package org.ian.soonseo;

import java.util.concurrent.*;

public class RunnableTaskWrapper implements TaskWrapper {

    private final Runnable task;
    private final CompletableFuture<Void> future;
    private final TaskQueueConfig config;

    public RunnableTaskWrapper(Runnable task, CompletableFuture<Void> future, TaskQueueConfig config) {
        this.task = task;
        this.future = future;
        this.config = config;
    }

    @Override
    public void execute() throws Exception {
        try {
            if (config.getTaskTimeout() != null) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Void> timeoutFuture = executor.submit(() -> {
                    task.run();
                    return null;
                });

                timeoutFuture.get(config.getTaskTimeout().toMillis(), TimeUnit.MILLISECONDS);
                executor.shutdown();
            } else {
                task.run();
            }

            future.complete(null);

        } catch (TimeoutException e) {
            future.completeExceptionally(new TimeoutException("Task execution timed out"));
            throw e;
        } catch (Exception e) {
            future.completeExceptionally(e);
            throw e;
        }
    }

}
