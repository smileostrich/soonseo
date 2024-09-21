package org.ian.soonseo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JobTest {

    private static final Logger logger = LogManager.getLogger(JobTest.class);

    private Job job;
    private final String SAMPLE_FUNC = "SampleFunction";
    private final String SAMPLE_ARGS = "arg1,arg2";


    @BeforeEach
    public void setUp() {
        job = new Job(SAMPLE_FUNC, SAMPLE_ARGS, () -> logger.info("Task executed"));
    }

    @Test
    public void testJobInitialization() {
        assertNotNull(job.getKey());
        assertEquals(SAMPLE_FUNC, job.getFunction());
        assertEquals(SAMPLE_ARGS, job.getArgs());
        assertEquals(JobStatus.QUEUED, job.getStatus());
    }

    @Test
    public void testJobExecutionSuccess() {
        job.run();
        job.setStartedTime(System.nanoTime());
        job.setCompletedTime(System.nanoTime());

        assertEquals(JobStatus.QUEUED, job.getStatus());
    }

    @Test
    public void testJobExceptionHandling() {
        Job jobWithError = new Job("ErrorFunction", SAMPLE_ARGS, () -> {
            throw new RuntimeException("Execution failed");
        });

        assertThrows(RuntimeException.class, jobWithError::run);
        assertEquals(JobStatus.QUEUED, jobWithError.getStatus());
    }
}
