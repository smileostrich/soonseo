package org.ian.soonseo.queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AsyncTaskQueueTest {

    Logger logger = LogManager.getLogger(AsyncTaskQueueTest.class);

    private AsyncTaskQueue taskQueue;
    private TaskQueueConfig config;

    @BeforeEach
    public void setUp() {
        config = new TaskQueueConfig.Builder()
                .setMaxRetries(3)
                .setTaskTimeout(Duration.ofMillis(100))
                .setEnableLogging(true)
                .build();
        taskQueue = new AsyncTaskQueue(config);
    }

    @AfterEach
    public void tearDown() {
        taskQueue.shutdown();
    }

    @Test
    public void testSubmitRunnable() throws Exception {
        CompletableFuture<Void> future = (CompletableFuture<Void>) taskQueue.submit(() -> logger.info("Task Completed"));

        future.get(500, TimeUnit.MILLISECONDS);
        assertTrue(future.isDone());
    }

    @Test
    public void testSubmitCallable() throws Exception {
        CompletableFuture<String> future = taskQueue.submit(() -> {
            Thread.sleep(50);
            return "Task Completed";
        });

        String result = future.get(200, TimeUnit.MILLISECONDS);
        assertEquals("Task Completed", result);
    }

    @Test
    public void testTimeout() {
        Callable<String> longRunningTask = () -> {
            Thread.sleep(500); // Exceeds the timeout of 100ms
            return "Task Completed";
        };

        CompletableFuture<String> future = taskQueue.submit(longRunningTask);
        assertThrows(ExecutionException.class, future::get);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testTaskRetries() throws InterruptedException {
        AtomicInteger attemptCounter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(4); // We expect 4 total attempts (initial + 3 retries)

        taskQueue.submit(() -> {
            attemptCounter.incrementAndGet();
            latch.countDown();
            throw new RuntimeException("Simulated task failure");
        });

        boolean allRetriesFinished = latch.await(1, TimeUnit.SECONDS);

        assertTrue(allRetriesFinished, "The task did not retry the expected number of times.");
        assertEquals(4, attemptCounter.get(), "Task retry count did not match the expected value.");
    }

    @Test
    public void testShutdown() {
        CompletableFuture<Void> future = (CompletableFuture<Void>) taskQueue.submit(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        taskQueue.shutdown();

        assertTrue(future.isDone());
    }

    @Test
    public void testMetrics() {
        taskQueue.submit(() -> System.out.println("Task 1"));
        taskQueue.submit(() -> System.out.println("Task 2"));

        TaskQueueMetrics metrics = taskQueue.getMetrics();
        assertEquals(2, metrics.getSubmittedTasks());
    }

}
