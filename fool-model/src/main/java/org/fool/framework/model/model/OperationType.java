package org.fool.framework.model.model;

public enum OperationType {
    DANYMIC(0),
    REFLECTION(1);

    private final int code;

    OperationType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
