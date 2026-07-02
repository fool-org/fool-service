package org.fool.framework.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GlobalSqlContext {
    private static final Map<Class<?>, String> TABLE = new ConcurrentHashMap<>();
    private static String conStr;

    private GlobalSqlContext() {
    }

    public static String getConStr() {
        return conStr;
    }

    public static void setConStr(String conStr) {
        GlobalSqlContext.conStr = conStr;
    }

    public static void regSqlCon(Class<?> type, String keyName, String sqlCon) {
        TABLE.put(type, sqlCon);
    }

    public static String getConStr(Class<?> type, String keyName) {
        return TABLE.getOrDefault(type, conStr);
    }
}
