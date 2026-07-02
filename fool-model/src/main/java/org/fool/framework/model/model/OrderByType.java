package org.fool.framework.model.model;

public enum OrderByType {
    ASC(0),
    DESC(1);

    private final int code;

    OrderByType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
