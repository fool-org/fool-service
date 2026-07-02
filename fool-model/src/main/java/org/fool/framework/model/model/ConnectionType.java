package org.fool.framework.model.model;

public enum ConnectionType {
    DEFAULT(0),
    SYSTEM(1),
    APP_SYS(2),
    CURRENT(3),
    MODEL_SYS(4);

    private final int code;

    ConnectionType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
