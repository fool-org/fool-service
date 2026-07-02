package org.fool.framework.event;

public enum MsgNotifyType {
    User(0),
    Role(1),
    Dep(2),
    Company(3),
    Auth(4),
    All(5);

    private final int code;

    MsgNotifyType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
