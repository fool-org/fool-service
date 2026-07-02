package org.fool.framework.report;

public enum OrderType {
    ASC(0),
    DESC(1);

    private final int code;

    OrderType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
