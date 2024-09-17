package org.ian.soonseo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

public class TaskQueueMetrics {

    private static final Logger logger = LogManager.getLogger(TaskQueueMetrics.class);

    private final AtomicLong submittedTasks = new AtomicLong();
    private final AtomicLong completedTasks = new AtomicLong();
    private final AtomicLong failedTasks = new AtomicLong();
    private final AtomicLong totalExecutionTimeNanos = new AtomicLong();
    private final boolean enableLogging;

    public TaskQueueMetrics(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public void incrementSubmitted() {
        long count = submittedTasks.incrementAndGet();
        if (enableLogging && logger.isTraceEnabled())
            logger.trace("Submitted tasks incremented: {}", count);
    }

    public void incrementCompleted(long executionTimeNanos) {
        long count = completedTasks.incrementAndGet();
        totalExecutionTimeNanos.addAndGet(executionTimeNanos);
        if (enableLogging && logger.isTraceEnabled()) {
            logger.trace("Completed tasks incremented: {} | Execution time: {} ns", count, executionTimeNanos);
        }
    }

    public void incrementFailed() {
        long count = failedTasks.incrementAndGet();
        if (enableLogging && logger.isTraceEnabled())
            logger.trace("Failed tasks incremented: {}", count);
    }

    public long getSubmittedTasks() {
        return submittedTasks.get();
    }

    public long getCompletedTasks() {
        return completedTasks.get();
    }

    public long getFailedTasks() {
        return failedTasks.get();
    }

    public long getTotalExecutionTimeNanos() {
        return totalExecutionTimeNanos.get();
    }

    public double getAverageExecutionTimeNanos() {
        long completedCount = completedTasks.get();
        if (completedCount == 0)
            return 0.0;

        return (double) totalExecutionTimeNanos.get() / completedCount;
    }

}
