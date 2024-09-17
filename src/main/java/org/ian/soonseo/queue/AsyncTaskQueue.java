package org.ian.soonseo.queue;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class AsyncTaskQueue implements TaskQueue {

    private static final Logger logger = LogManager.getLogger(AsyncTaskQueue.class);

    private final Disruptor<TaskEvent> disruptor;
    private final RingBuffer<TaskEvent> ringBuffer;
    private final TaskQueueMetrics metrics;
    private final TaskQueueConfig config;
    private final boolean enableLogging;

    public AsyncTaskQueue(TaskQueueConfig config) {
        this.config = config;
        this.enableLogging = config.isEnableLogging();
        this.metrics = new TaskQueueMetrics(enableLogging);

        ThreadFactory threadFactory = Thread.ofVirtual().factory();
        TaskEventFactory eventFactory = new TaskEventFactory();

        int bufferSize = config.getBufferSize();

        this.disruptor = new Disruptor<>(
                eventFactory,
                bufferSize,
                threadFactory,
                ProducerType.MULTI,
                new BusySpinWaitStrategy()
        );

        TaskEventHandler eventHandler = new TaskEventHandler(metrics, config);
        disruptor.handleEventsWith(eventHandler);
        disruptor.start();
        this.ringBuffer = disruptor.getRingBuffer();

        if (enableLogging && logger.isDebugEnabled())
            logger.debug("AsyncTaskQueue initialized with BusySpinWaitStrategy and buffer size: {}", bufferSize);
    }

    @Override
    public Future<?> submit(Runnable task) {
        metrics.incrementSubmitted();
        CompletableFuture<Void> future = new CompletableFuture<>();
        publishEvent(new RunnableTaskWrapper(task, future, config));

        return future;
    }

    @Override
    public <T> CompletableFuture<T> submit(Callable<T> task) {
        metrics.incrementSubmitted();
        CompletableFuture<T> future = new CompletableFuture<>();
        publishEvent(new CallableTaskWrapper<>(task, future, config));

        return future;
    }

    private void publishEvent(TaskWrapper taskWrapper) {
        long sequence = ringBuffer.next();
        try {
            TaskEvent event = ringBuffer.get(sequence);
            event.setTaskWrapper(taskWrapper);
        } finally {
            ringBuffer.publish(sequence);
            if (enableLogging && logger.isTraceEnabled())
                logger.trace("Task published at sequence: {}", sequence);
        }
    }

    @Override
    public void shutdown() {
        try {
            disruptor.shutdown(5, TimeUnit.SECONDS); // Graceful shutdown with 5 seconds timeout
            if (enableLogging)
                logger.info("Disruptor shut down successfully.");
        } catch (TimeoutException e) {
            if (enableLogging)
                logger.error("Disruptor did not shut down in time. Forcing shutdown.", e);
        }
    }

    public TaskQueueMetrics getMetrics() {
        return metrics;
    }

}
