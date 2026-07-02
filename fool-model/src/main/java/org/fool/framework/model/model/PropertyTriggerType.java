package org.fool.framework.model.model;

public enum PropertyTriggerType {
    SET(0),
    ITEMS_ADD(1),
    ITEMS_SET(2),
    ITEMS_DELETE(3);

    private final int code;

    PropertyTriggerType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
