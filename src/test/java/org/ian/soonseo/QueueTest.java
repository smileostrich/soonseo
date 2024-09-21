package org.ian.soonseo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ian.soonseo.metrics.CapturedJobMetrics;
import org.ian.soonseo.metrics.CapturedQueueMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {

    private final Logger logger = LogManager.getLogger(QueueTest.class);

    private Queue queue;

    @BeforeEach
    public void setup() {
        queue = new Queue(new Config(1024, 2, 3, 100));
    }

    @Test
    public void testJobSubmission() {
        Job job = new Job("SampleFunction", "arg1,arg2", () -> logger.info("Job executed"));
        queue.submit(job);

        List<CapturedJobMetrics> jobMetrics = queue.captureJobMetrics();
        assertEquals(1, jobMetrics.size());
        assertEquals("SampleFunction", jobMetrics.getFirst().function());
    }

    @Test
    public void testQueueMetrics() {
        String func1 = "SampleFunction1";
        String func2 = "SampleFunction2";

        Job job1 = new Job(func1, "arg1,arg2", () -> logger.info("{} executed", func1));
        Job job2 = new Job(func2, "arg3,arg4", () -> logger.info("{} executed", func2));
        queue.submit(job1);
        queue.submit(job2);

        CapturedQueueMetrics metrics = queue.captureQueueMetrics();
        assertNotNull(metrics);
        assertEquals(2, metrics.queued());
    }

    @Test
    public void testQueueRetryAndRejection() {
        Config config = new Config(1, 2, 3, 100);
        Queue smallQueue = new Queue(config);

        smallQueue.getWorkers().forEach(worker -> worker.setProcessDelay(500));

        Job job1 = new Job("SampleFunction1", "arg1,arg2", () -> logger.info("Job 1 executed"));
        Job job2 = new Job("SampleFunction2", "arg3,arg4", () -> logger.info("Job 2 executed"));

        smallQueue.submit(job1);
        smallQueue.submit(job2);

        CapturedQueueMetrics metrics = smallQueue.captureQueueMetrics();
        assertNotNull(metrics);
        assertEquals(2, metrics.queued());
    }

    @Test
    public void testJobDiscardAfterMaxRetries() {
        Config config = new Config(1, 2, 3, 50);
        Queue smallQueue = new Queue(config);

        smallQueue.getWorkers().forEach(worker -> worker.setProcessDelay(500));

        Job job1 = new Job("SampleFunction1", "arg1,arg2", () -> logger.info("Job 1 executed"));
        Job job2 = new Job("SampleFunction2", "arg3,arg4", () -> logger.info("Job 2 executed"));

        smallQueue.submit(job1);
        assertThrows(RejectedExecutionException.class, () -> smallQueue.submit(job2)); // This job should fail after 3 retries

        List<CapturedJobMetrics> jobMetrics = smallQueue.captureJobMetrics();
        assertEquals(1, jobMetrics.size());
        assertEquals("SampleFunction1", jobMetrics.getFirst().function());
    }


    @Test
    public void testShutdown() {
        try {
            queue.shutdown();
        } catch (Exception ignored) {
            fail("Shutdown should not throw an exception");
        }
    }

}
