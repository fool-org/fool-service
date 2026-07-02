package org.fool.framework.query;

public enum OrderType {
    ASC(0),
    DESC(1),
    NULL(2);

    private final int code;

    OrderType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
