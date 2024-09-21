package org.ian.soonseo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ian.soonseo.exception.JobProcessingException;
import org.ian.soonseo.metrics.CapturedJobMetrics;
import org.ian.soonseo.metrics.CapturedQueueMetrics;
import org.ian.soonseo.metrics.CapturedWorkerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class SampleUsageTest {

    private final Logger logger = LogManager.getLogger(SampleUsageTest.class);
    private Queue queue;

    @BeforeEach
    public void setUp() {
        Config config = new Config(1024, 2, 3, 100);  // 1024 buffer size, 2 workers, 3 retries, 100ms backoff
        queue = new Queue(config);
    }

    @Test
    public void testSubmitAndProcessJobs() throws JobProcessingException, InterruptedException {
        Job job1 = new Job("Function1", "arg1,arg2", () -> System.out.println("Job 1 executed"));
        Job job2 = new Job("Function2", "arg3,arg4", () -> System.out.println("Job 2 executed"));

        queue.submit(job1);
        queue.submit(job2);

        Thread.sleep(500);

        CapturedQueueMetrics queueMetrics = queue.captureQueueMetrics();
        assertNotNull(queueMetrics);
        assertEquals(2, queueMetrics.active());
        assertEquals(2, queueMetrics.queued());

        List<CapturedJobMetrics> jobMetrics = queue.captureJobMetrics();
        assertEquals(2, jobMetrics.size());

        for (CapturedJobMetrics metrics : jobMetrics) {
            assertEquals(JobStatus.COMPLETED, metrics.status());
            System.out.println("Job " + metrics.key() + " completed");
        }
    }

    @Test
    public void testJobRetry() throws JobProcessingException, InterruptedException {
        Config smallBufferConfig = new Config(1, 2, 3, 50);
        Queue retryQueue = new Queue(smallBufferConfig);

        Job job1 = new Job("Function1", "arg1,arg2", () -> System.out.println("Job 1 executed"));
        Job job2 = new Job("Function2", "arg3,arg4", () -> System.out.println("Job 2 executed"));

        retryQueue.submit(job1);
        retryQueue.submit(job2);

        Thread.sleep(1000);

        List<CapturedJobMetrics> jobMetrics = retryQueue.captureJobMetrics();
        assertEquals(2, jobMetrics.size());

        CapturedQueueMetrics queueMetrics = retryQueue.captureQueueMetrics();
        assertEquals(2, queueMetrics.queued());
    }

    @Test
    public void testJobFailure() throws JobProcessingException {
        Job failingJob = new Job("Function1", "arg1,arg2", () -> {
            throw new RuntimeException("Job failure simulation");
        });

        queue.submit(failingJob);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<CapturedJobMetrics> jobMetrics = queue.captureJobMetrics();
        assertEquals(1, jobMetrics.size());
        assertEquals(JobStatus.FAILED, jobMetrics.get(0).status());
    }

    @Test
    public void testWorkerMetrics() throws JobProcessingException, InterruptedException {
        Job job = new Job("Function1", "arg1,arg2", () -> System.out.println("Job executed"));

        queue.submit(job);

        Thread.sleep(500);

        List<CapturedWorkerMetrics> workerMetrics = queue.captureWorkerMetrics();
        assertEquals(2, workerMetrics.size());

        for (CapturedWorkerMetrics metrics : workerMetrics) {
            assertTrue(metrics.completed() >= 0);
            assertEquals(0, metrics.failed());
            logger.info("Worker {} processed jobs: {}", metrics.workerId(), metrics.completed());
        }
    }

}
