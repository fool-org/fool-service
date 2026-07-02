package org.fool.framework.query;

public enum AddQueryTable {
    Success(0),
    NoRelation(1),
    Exists(2);

    private final int code;

    AddQueryTable(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
