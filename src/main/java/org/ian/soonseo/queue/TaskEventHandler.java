package org.ian.soonseo.queue;

import com.lmax.disruptor.EventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskEventHandler implements EventHandler<TaskEvent> {

    private static final Logger logger = LogManager.getLogger(TaskEventHandler.class);
    private final TaskQueueMetrics metrics;
    private final TaskQueueConfig config;
    private final boolean enableLogging;

    public TaskEventHandler(TaskQueueMetrics metrics, TaskQueueConfig config) {
        this.metrics = metrics;
        this.config = config;
        this.enableLogging = config.isEnableLogging();
    }

    @Override
    public void onEvent(TaskEvent event, long sequence, boolean endOfBatch) {
        TaskWrapper taskWrapper = event.getTaskWrapper();
        if (taskWrapper == null)
            return;

        long startTime = System.nanoTime();
        try {
            processTaskWithRetries(taskWrapper);
            metrics.incrementCompleted(System.nanoTime() - startTime);
        } catch (Exception e) {
            if (enableLogging) {
                logger.error("Task failed after retries at sequence: {}", sequence, e);
            }
            metrics.incrementFailed();
            if (config.getErrorHandler() != null)
                config.getErrorHandler().handle(e);
        } finally {
            event.setTaskWrapper(null); // Clear task reference to avoid memory leaks
        }
    }

    private void processTaskWithRetries(TaskWrapper taskWrapper) throws Exception {
        int attempts = 0;
        while (attempts <= config.getMaxRetries()) {
            try {
                taskWrapper.execute();
                return;
            } catch (Exception e) {
                if (++attempts > config.getMaxRetries()) {
                    throw e;
                }
                if (enableLogging) {
                    logger.warn("Task failed on attempt {}/{}. Retrying...", attempts, config.getMaxRetries());
                }
            }
        }
    }

}
