package org.ian.soonseo;

@FunctionalInterface
public interface ErrorHandler {

    void handle(Exception e);

}
