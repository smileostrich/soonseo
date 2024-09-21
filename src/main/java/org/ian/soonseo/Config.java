package org.ian.soonseo;

public record Config(int bufferSize, int workerSize, int maxRetries, long backoffTime) {
}
