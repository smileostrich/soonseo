package org.ian.soonseo;

import com.lmax.disruptor.EventFactory;

public class TaskEventFactory implements EventFactory<TaskEvent> {

    @Override
    public TaskEvent newInstance() {
        return new TaskEvent(); // Pre-allocate TaskEvent objects
    }

}
