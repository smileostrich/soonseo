package org.ian.soonseo.metrics;

public record CapturedQueueMetrics(
        long active,
        long queued,
        long scheduled) {
}
