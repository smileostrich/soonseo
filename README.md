# soonseo
**soonseo** is a simple & extremely fast task queuing library for Java that leverages LMAX Disruptor and virtual threads to handle high-performance async tasks with minimal overhead.

## Features
- High throughput: Leverages the LMAX Disruptor for extremely fast and efficient event processing.
- Virtual threads support: Fully supports JDK 21+ virtual threads, providing lightweight concurrency with minimal resource usage.
- Configurable: Allows you to customize task queue behavior, including:
  - Task timeouts
  - Retry logic with configurable maximum retries
  - Error handling
  - Logging
  - Buffer size for task queuing
- Flexible task submission: Submit Runnable or Callable tasks for asynchronous execution.
- Graceful shutdown: Safely shut down the task queue without losing tasks in progress.
 

## Installation

To use soonseo in your project, add the following dependency to your Maven `pom.xml`:

### For maven
```xml
<dependency>
    <groupId>org.ian.soonseo</groupId>
    <artifactId>soonseo</artifactId>
    <version>0.1.0</version>
</dependency>
```

### For Gradle
```groovy
implementation 'org.ian.soonseo:soonseo:0.1.0'
```

## Usage

### Basic Example

Here’s a basic example of how to configure and use soonseo

```java
TaskQueueConfig config = new TaskQueueConfig.Builder()
        .setMaxRetries(3)
        .setTaskTimeout(Duration.ofSeconds(5))
        .setEnableLogging(false)
        .setBufferSize(1024)
        .setErrorHandler(e -> System.err.println("Error occurred: " + e.getMessage())) // Custom error handler
        .build();

TaskQueue taskQueue = new AsyncTaskQueue(config);

// Submitting a simple task
taskQueue.submit(() -> {
// Task logic
});

// Shutting down the task queue
taskQueue.shutdown();
```

### Callable Example

If your task needs to return a result, you can submit a Callable and handle the result asynchronously
```java
CompletableFuture<String> future = taskQueue.submit(() -> {
    Thread.sleep(1000);
    return "Task Completed!";
});

try {
    // Wait for the result
    String result = future.get();
    System.out.println("Result: " + result);
} catch (InterruptedException | ExecutionException e) {
}
```

### Handling Task Timeouts

soonseo allows you to set a task timeout to automatically terminate tasks that exceed the specified duration
```java
TaskQueueConfig config = new TaskQueueConfig.Builder()
    .setTaskTimeout(Duration.ofSeconds(2))
    .build();

CompletableFuture<Void> future = taskQueue.submit(() -> {
    Thread.sleep(5000);
    System.out.println("This line will not be reached due to the timeout.");
});

try {
    future.get();  // This will throw a TimeoutException
} catch (ExecutionException e) {
}
```


## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change. Contributions to improve documentation, test coverage, or features are highly appreciated.

### How to Contribute

1. Fork the repository
2. Create a new feature branch (git checkout -b feature-branch)
3. Commit your changes (git commit -am 'Add new feature')
4. Push to the branch (git push origin feature-branch)
4. Open a Pull Request

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.
