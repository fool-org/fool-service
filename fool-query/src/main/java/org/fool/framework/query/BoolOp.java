package org.fool.framework.query;

public enum BoolOp {
    AND(" And ", "并且"),
    OR(" OR ", "或者");

    private final String dbName;
    private final String showName;

    BoolOp(String dbName, String showName) {
        this.dbName = dbName;
        this.showName = showName;
    }

    public String getDbName() {
        return dbName;
    }

    public String getShowName() {
        return showName;
    }
}
