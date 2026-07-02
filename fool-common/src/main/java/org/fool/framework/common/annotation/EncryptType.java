package org.fool.framework.common.annotation;

public enum EncryptType {
    NONE(0),
    MD5(1),
    RADOM_DECS(2);

    private final int code;

    EncryptType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
