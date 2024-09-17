package org.ian.soonseo;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface TaskQueue {

    Future<?> submit(Runnable task);

    <T> CompletableFuture<T> submit(Callable<T> task);

    void shutdown();

}
