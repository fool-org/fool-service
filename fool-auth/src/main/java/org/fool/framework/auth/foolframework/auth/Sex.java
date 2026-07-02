package org.fool.framework.auth.foolframework.auth;

public enum Sex {
    Male(0),
    Female(1);

    private final int code;

    Sex(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
