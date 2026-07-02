package org.fool.framework.event;

public enum MsgState {
    Generate(0),
    Push(1),
    Readed(2),
    Dealed(3),
    TimeOut(4);

    private final int code;

    MsgState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
