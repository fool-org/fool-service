package org.fool.framework.model.model;

/**
 * 操作基本类型
 */
public enum OperationBaseType {
    CREATE(0),
    UPDATE(1),
    DELETE(2),
    NULL(4),
    ASSEBMLY(5),
    WCF(6),
    JSONPOST(7),
    JSONGET(8);

    private final int code;

    OperationBaseType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
