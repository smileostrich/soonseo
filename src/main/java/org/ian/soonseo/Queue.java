package org.ian.soonseo;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.ian.soonseo.metrics.CapturedJobMetrics;
import org.ian.soonseo.metrics.CapturedQueueMetrics;
import org.ian.soonseo.metrics.CapturedWorkerMetrics;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Queue {

    private final RingBuffer<JobEvent> ringBuffer;
    private final List<Worker> workers;
    private final ConcurrentHashMap<String, Job> jobMap = new ConcurrentHashMap<>();
    private final AtomicLong queuedJobs = new AtomicLong(0);
    private final ExecutorService executorService;
    private final int maxRetries;
    private final long backoffTime;

    public Queue(Config config) {
        ThreadFactory threadFactory = Thread.ofVirtual().factory();
        Disruptor<JobEvent> disruptor = new Disruptor<>(
                JobEvent::new,
                config.bufferSize(),
                threadFactory,
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );

        workers = new CopyOnWriteArrayList<>();
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.maxRetries = config.maxRetries();
        this.backoffTime = config.backoffTime();

        for (int i = 0; i < config.workerSize(); i++) {
            Worker worker = new Worker();
            workers.add(worker);
        }

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {
            Worker worker = workers.get((int) (sequence % workers.size()));
            worker.process(event.getJob());
        });

        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public void submit(Job job) throws RejectedExecutionException {
        int retryCount = 0;
        long backoff = backoffTime;
        boolean submitted = false;

        while (retryCount <= maxRetries && !submitted) {
            try {
                long sequence = ringBuffer.tryNext();
                ringBuffer.get(sequence).setJob(job);
                ringBuffer.publish(sequence);
                queuedJobs.incrementAndGet();
                jobMap.put(job.getKey(), job);
                submitted = true;
            } catch (InsufficientCapacityException e) {
                retryCount++;
                workers.forEach(Worker::incrementRetries);

                if (retryCount > maxRetries)
                    throw new RejectedExecutionException("Failed to submit job after " + maxRetries + " retries: " + job.getKey());

                try {
                    Thread.sleep(backoff);
                    backoff *= 2; // Exponential backoff
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted for job: " + job.getKey(), ex);
                }
            }
        }
    }

    public List<CapturedJobMetrics> captureJobMetrics() {
        return jobMap.values().stream()
                .map(job -> new CapturedJobMetrics(
                        job.getKey(),
                        job.getFunction(),
                        job.getArgs(),
                        job.getQueuedTime(),
                        job.getStartedTime(),
                        job.getCompletedTime(),
                        job.getStatus()))
                .collect(Collectors.toList());
    }

    public List<CapturedWorkerMetrics> captureWorkerMetrics() {
        return workers.stream()
                .map(Worker::captureMetrics)
                .collect(Collectors.toList());
    }

    public CapturedQueueMetrics captureQueueMetrics() {
        long activeWorkers = workers.size();
        long completedJobs = workers.stream().mapToLong(Worker::getCompletedJobs).sum();
        long failedJobs = workers.stream().mapToLong(Worker::getFailedJobs).sum();
        long scheduledJobs = queuedJobs.get() - completedJobs - failedJobs;

        return new CapturedQueueMetrics(activeWorkers, queuedJobs.get(), scheduledJobs);
    }

    public void shutdown() throws InterruptedException, TimeoutException {
        executorService.shutdownNow();
        if (!executorService.awaitTermination(5, TimeUnit.SECONDS))
            throw new TimeoutException("Executor service did not terminate in the expected time");
    }

}
