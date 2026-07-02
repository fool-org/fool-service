package org.fool.framework.app;

public enum AppType {
    Web(0),
    WinForm(1),
    Android(2),
    iOS(3),
    Service(4),
    Sensor(5);

    private final int code;

    AppType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
