package com.server.extensions.common.event;

/**
 * Created by wuyingtan on 2016/12/12.
 */
public class EventBase {
    private EventType value;
    private boolean isAsync;

    public EventBase(EventType value) {
        this.value = value;
        isAsync = false;
    }

    public EventType getValue() {
        return value;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync() {
        this.isAsync = true;
    }
}
