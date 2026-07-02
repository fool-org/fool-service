package org.fool.framework.report;

public enum StaticType {
    Max(0),
    Min(1),
    Avg(2),
    Sum(3),
    Ignore(4);

    private final int code;

    StaticType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
