package org.ian.soonseo;

import java.time.Duration;

public class TaskQueueConfig {

    private final ErrorHandler errorHandler;
    private final Duration taskTimeout;
    private final boolean enableLogging;
    private final int bufferSize;
    private final int maxRetries;

    private TaskQueueConfig(Builder builder) {
        this.errorHandler = builder.errorHandler;
        this.enableLogging = builder.enableLogging;
        this.taskTimeout = builder.taskTimeout;
        this.maxRetries = builder.maxRetries;
        this.bufferSize = builder.bufferSize;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public Duration getTaskTimeout() {
        return taskTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public static class Builder {
        private int maxRetries = 3;
        private boolean enableLogging = false;
        private ErrorHandler errorHandler = e -> {};
        private Duration taskTimeout = null;
        private int bufferSize = 1024;

        public Builder setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder setEnableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        public Builder setErrorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public Builder setTaskTimeout(Duration taskTimeout) {
            this.taskTimeout = taskTimeout;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public TaskQueueConfig build() {
            return new TaskQueueConfig(this);
        }
    }

}
