package org.fool.framework.report;

public enum CalDirection {
    Null(0),
    Row(1),
    Column(2);

    private final int code;

    CalDirection(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
