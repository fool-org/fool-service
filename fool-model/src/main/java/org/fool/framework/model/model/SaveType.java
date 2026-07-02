package org.fool.framework.model.model;

public enum SaveType {
    UNKNOWN(0),
    EXISTS(1),
    UN_EXISTS(2);

    private final int code;

    SaveType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
