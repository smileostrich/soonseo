package org.ian.soonseo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ian.soonseo.exception.JobProcessingException;
import org.ian.soonseo.metrics.CapturedWorkerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WorkerTest {

    private final Logger logger = LogManager.getLogger(WorkerTest.class);

    private Worker worker;
    private Job job;

    @BeforeEach
    public void setUp() {
        worker = new Worker();
        job = new Job("SampleFunction", "arg1,arg2", () -> logger.info("Task executed"));
    }

    @Test
    public void testWorkerProcessing() throws Exception {
        worker.process(job);
        assertEquals(JobStatus.COMPLETED, job.getStatus());
        assertEquals(1, worker.getCompletedJobs());
    }

    @Test
    public void testCaptureWorkerMetrics() throws Exception {
        worker.process(job);
        CapturedWorkerMetrics metrics = worker.captureMetrics();
        assertNotNull(metrics);
        assertEquals(1, metrics.completed());
        assertTrue(metrics.uptime() > 0);
    }

    @Test
    public void testWorkerFailureHandling() {
        Job failingJob = new Job("FailFunction", "arg1,arg2", () -> {
            throw new RuntimeException("Job failed");
        });

        try {
            worker.process(failingJob);
        } catch (JobProcessingException | InterruptedException e) {
            // Exception is expected, no need to fail the test
        }

        // Verify the job was marked as failed
        assertEquals(JobStatus.FAILED, failingJob.getStatus());
        // Verify the worker tracked the failed job
        assertEquals(1, worker.getFailedJobs());
    }


    @Test
    public void testWorkerUptime() throws Exception {
        long uptimeBefore = worker.getUptime();
        worker.process(job);
        long uptimeAfter = worker.getUptime();

        assertTrue(uptimeAfter > uptimeBefore);
    }

    @Test
    public void testWorkerRetryTracking() {
        worker.incrementRetries();
        worker.incrementRetries();

        CapturedWorkerMetrics metrics = worker.captureMetrics();
        assertEquals(2, metrics.retried());
    }

}
