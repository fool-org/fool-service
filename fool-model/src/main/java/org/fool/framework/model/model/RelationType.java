package org.fool.framework.model.model;

public enum RelationType {
    One2Many(0),
    Many2Many(1),
    Many2One(2),
    Recurve(3);

    private final int code;

    RelationType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
