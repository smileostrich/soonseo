# soonseo
**soonseo** is a simple & extremely fast task queuing library for Java that leverages LMAX Disruptor and virtual threads to handle high-performance async tasks with minimal overhead.

## Features
- High throughput task queue
- Virtual thread support (Java 21+)
- Configurable task timeouts, retries, logging, buffer size, etc. 

## Installation

To use soonseo in your project, add the following dependency to your Maven `pom.xml`:

```xml

<dependency>
    <groupId>org.ian.soonseo</groupId>
    <artifactId>soonseo</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

Example usage of soonseo:

```java
TaskQueueConfig config = new TaskQueueConfig.Builder()
    .setMaxRetries(3)
    .setTaskTimeout(Duration.ofSeconds(5))
    .setEnableLogging(false)
    .build();

TaskQueue taskQueue = new AsyncTaskQueue(config);
    taskQueue.submit(() -> {
    // Your task code here
});
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.


## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE](LICENSE) file for details.
```

