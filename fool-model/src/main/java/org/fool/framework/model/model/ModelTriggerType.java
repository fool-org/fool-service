package org.fool.framework.model.model;

public enum ModelTriggerType {
    CREATE(0),
    SAVE(1),
    DELETE(2),
    BEFORE_CREATE(3),
    BEFORE_SAVE(4),
    BEFORE_DELETE(5);

    private final int code;

    ModelTriggerType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
