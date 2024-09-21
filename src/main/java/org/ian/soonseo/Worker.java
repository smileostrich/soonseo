package org.ian.soonseo;

import org.ian.soonseo.exception.JobProcessingException;
import org.ian.soonseo.metrics.CapturedWorkerMetrics;
import org.ian.soonseo.utils.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

public class Worker {

    private final String workerId;
    private final AtomicLong completedJobs = new AtomicLong();
    private final AtomicLong failedJobs = new AtomicLong();
    private final AtomicLong retriedJobs = new AtomicLong();
    private final long startTime = System.nanoTime();
    private long processDelay = 0;

    public Worker() {
        this.workerId = IdGenerator.genId();
    }

    public void setProcessDelay(long delay) {
        this.processDelay = delay;
    }

    public void process(Job job) throws JobProcessingException, InterruptedException {
        try {
            job.setStartedTime(System.nanoTime());
            job.setStatus(JobStatus.STARTED);

            // Simulate job processing delay (if applicable)
            Thread.sleep(processDelay);

            // Run the job task
            job.run();
            job.setCompletedTime(System.nanoTime());
            job.setStatus(JobStatus.COMPLETED);
            completedJobs.incrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedException("Job processing interrupted for job: " + job.getKey());
        } catch (RuntimeException e) {
            job.setStatus(JobStatus.FAILED);
            failedJobs.incrementAndGet();
            throw new JobProcessingException("Job failed during execution: " + job.getKey(), e);
        }
    }

    public void incrementRetries() {
        retriedJobs.incrementAndGet();
    }

    public long getCompletedJobs() {
        return completedJobs.get();
    }

    public long getFailedJobs() {
        return failedJobs.get();
    }

    public long getRetriedJobs() {
        return retriedJobs.get();
    }

    public long getUptime() {
        return System.nanoTime() - startTime;
    }

    public CapturedWorkerMetrics captureMetrics() {
        return new CapturedWorkerMetrics(
                workerId,
                completedJobs.get(),
                retriedJobs.get(),
                failedJobs.get(),
                getUptime()
        );
    }

}
