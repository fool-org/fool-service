package org.fool.framework.dao;

enum SqlOperation {
    INSERT("insert"),
    UPDATE_AFTER_INSERT("updateafterinsert"),
    UPDATE("update"),
    UPDATE_AFTER_UPDATE("updateafterupdate"),
    DELETE("delete"),
    EXCUTE("excute");

    private final String legacyName;

    SqlOperation(String legacyName) {
        this.legacyName = legacyName;
    }

    String legacyName() {
        return legacyName;
    }
}
