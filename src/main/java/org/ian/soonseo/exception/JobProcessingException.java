package org.ian.soonseo.exception;

public class JobProcessingException extends Exception {
    public JobProcessingException(String message) {
        super(message);
    }

    public JobProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
