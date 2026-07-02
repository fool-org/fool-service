package org.fool.framework.common.annotation;

public enum DynamicOperationType {
    ADD(1),
    SUB(2);

    private final int code;

    DynamicOperationType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
