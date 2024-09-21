package org.ian.soonseo.metrics;

import org.ian.soonseo.JobStatus;

public record CapturedJobMetrics(
        String key,
        String function,
        String args,
        long queuedTime,
        long startedTime,
        long completedTime,
        JobStatus status) {
}
