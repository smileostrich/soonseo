# soonseo
**soonseo** is a high-performance, lightweight task queuing library for Java that uses the LMAX Disruptor and virtual threads (JDK 21+) to provide asynchronous task processing. With low overhead and customizable retry logic, it's ideal for managing high-throughput task workloads with low-latency requirements.

## Features
- High throughput: Utilizes the LMAX Disruptor for low-latency, high-efficiency event processing.
- Virtual threads: Supports lightweight concurrency with virtual threads for scalable task processing.
- Configurable options:
  - Buffer size
  - Retry logic with customizable retry limits and backoff time
  - Task failure handling using custom exceptions
  - Worker metrics for monitoring task completion, failures, and retries
- Graceful shutdown: Ensures safe task queue termination with no loss of in-progress tasks


## Installation

To use Soonseo in your project, include the following dependency in your pom.xml (for Maven) or build.gradle (for Gradle)

### For maven
```xml
<dependency>
    <groupId>org.ian.soonseo</groupId>
    <artifactId>soonseo-core</artifactId>
    <version>0.2.1</version>
</dependency>
```

### For Gradle
```groovy
implementation 'org.ian.soonseo:soonseo-core:0.2.1'
```

## Usage

### Basic Example

Below is an example demonstrating how to configure and use the **Soonseo** task queue

```java
Config config = new Config(1024, 2, 3, 100);  // Buffer size, 2 workers, 3 retries, 100ms backoff
Queue queue = new Queue(config);

Job job1 = new Job("Task1", "arg1,arg2", () -> {
  // Task logic goes here
});

queue.submit(job1);

try {
    queue.shutdown();
} catch (InterruptedException | TimeoutException e) {
    logger.error("Queue shutdown interrupted or timed out: ");
}
```

### Callable Example

If your task needs to return a result, you can submit a Callable and handle the result asynchronously
```java
CompletableFuture<String> future = taskQueue.submit(() -> {
    Thread.sleep(1000);
    return "Task Completed!";
});

try {
    String result = future.get();
} catch (InterruptedException | ExecutionException e) {
}
```

### Handling Task Failures

In **Soonseo**, if a task throws an exception during execution, it will be marked as FAILED. You can use the job and worker metrics to track job status and retries.

```java
Job failingJob = new Job("TaskWithFailure", "arg1,arg2", () -> {
    throw new RuntimeException("Simulating a task failure");
});

try {
    queue.submit(failingJob);
} catch (RejectedExecutionException e) {
}

// Capturing job metrics to check failure status
List<CapturedJobMetrics> jobMetrics = queue.captureJobMetrics();
jobMetrics.forEach(metric -> System.out.println("Job " + metric.key() + " status: " + metric.status()));
```

### Worker Metrics

You can track the performance of workers, including the number of jobs they’ve completed, failed, or retried

```java
List<CapturedWorkerMetrics> workerMetrics = queue.captureWorkerMetrics();
for (CapturedWorkerMetrics metrics : workerMetrics) {
    logger.info("Worker " + metrics.workerId() + " completed jobs: " + metrics.completed());
    logger.info("Worker " + metrics.workerId() + " failed jobs: " + metrics.failed());
    logger.info("Worker " + metrics.workerId() + " retried jobs: " + metrics.retried());
}
```

### Handling Task Retries

**Soonseo** allows you to configure retry behavior with an exponential backoff strategy. If a task fails due to insufficient buffer capacity, it will be retried up to the configured maximum retries.

```java
Job job2 = new Job("TaskWithRetry", "arg3,arg4", () -> {
    System.out.println("Job 2 executed");
});

queue.submit(job2);  // Job is retried if the buffer is full or capacity is unavailable
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change. Contributions to improve documentation, test coverage, or features are highly appreciated.

### How to Contribute

1. Fork the repository
2. Create a new feature branch (git checkout -b feature-branch)
3. Commit your changes (git commit -am 'Add new feature')
4. Push to the branch (git push origin feature-branch)
5. Open a Pull Request

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.
