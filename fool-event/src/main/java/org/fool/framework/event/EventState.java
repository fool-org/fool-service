package org.fool.framework.event;

public enum EventState {
    IsRunning(0),
    Stopped(1);

    private final int code;

    EventState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
