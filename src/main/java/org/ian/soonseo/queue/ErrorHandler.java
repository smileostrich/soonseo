package org.ian.soonseo.queue;

@FunctionalInterface
public interface ErrorHandler {

    void handle(Exception e);

}
