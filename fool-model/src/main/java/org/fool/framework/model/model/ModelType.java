package org.fool.framework.model.model;

public enum ModelType {
    DYNAMIC(0),
    ABSTRACT_CLASS(1),
    ENUM(2);

    private final int code;

    ModelType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
