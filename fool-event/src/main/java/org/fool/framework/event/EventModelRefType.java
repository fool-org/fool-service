package org.fool.framework.event;

public enum EventModelRefType {
    SysModel(0),
    AppModel(1),
    DbModel(2);

    private final int code;

    EventModelRefType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
