package org.ian.soonseo;

import static org.ian.soonseo.utils.IdGenerator.genId;

public class Job {

    private final String key;
    private final String function;
    private final String args;
    private final Runnable task;

    private final long queuedTime;
    private long startedTime;
    private long completedTime;
    private JobStatus status;

    public Job(String function, String args, Runnable task) {
        this.key = genId();
        this.function = function;
        this.args = args;
        this.task = task;
        this.status = JobStatus.QUEUED;
        this.queuedTime = System.nanoTime();
    }

    public String getKey() { return key; }

    public String getFunction() { return function; }

    public String getArgs() { return args; }

    public long getQueuedTime() { return queuedTime; }

    public long getStartedTime() { return startedTime; }

    public void setStartedTime(long startedTime) { this.startedTime = startedTime; }

    public long getCompletedTime() { return completedTime; }

    public void setCompletedTime(long completedTime) { this.completedTime = completedTime; }

    public JobStatus getStatus() { return status; }

    public void setStatus(JobStatus status) { this.status = status; }

    public void run() {
        task.run();
    }

}
