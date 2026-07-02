package org.fool.framework.dao;

public enum OperationType {
    CREATE(0),
    SAVE(1),
    DELETE(2),
    DYNAMIC_UPDATE(3);

    private final int code;

    OperationType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
