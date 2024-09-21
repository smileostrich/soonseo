package org.ian.soonseo.metrics;

public record CapturedWorkerMetrics(
        String workerId,
        long completed,
        long retried,
        long failed,
        long uptime) {
}
