package org.fool.framework.model.model;

public enum LoadType {
    NULL(0),
    PARTIAL(1),
    COMPLETE(2),
    NO_OBJ(3);

    private final int code;

    LoadType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
