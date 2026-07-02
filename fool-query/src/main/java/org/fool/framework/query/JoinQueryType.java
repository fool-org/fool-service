package org.fool.framework.query;

public enum JoinQueryType {
    Parent(0),
    Items(1),
    All(2);

    private final int code;

    JoinQueryType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
