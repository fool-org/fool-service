package org.fool.framework.common.annotation;

public enum GenerationType {
    NEVER(0),
    ON_INSERT(1),
    ON_UPDATE(2),
    ON_INSERT_AND_UPDATE(3);

    private final int code;

    GenerationType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
