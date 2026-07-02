package org.fool.framework.model.model;

public enum CommandsType {
    SET_VALUE(0),
    SET_ACCESS(1),
    EXUTE_PROPRTY_MODEL_METHOD(2),
    EXUTE_OUT_MODEL_METHOD(3),
    SET_SOURCE(4),
    EXUTE_LIST_METHOD(5),
    FILTER(6),
    SET_PARAM_VALUE(7),
    SET_CON_STR_VALUE(8);

    private final int code;

    CommandsType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
