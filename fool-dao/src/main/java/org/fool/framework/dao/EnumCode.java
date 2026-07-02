package org.fool.framework.dao;

import java.lang.reflect.InvocationTargetException;

final class EnumCode {
    private EnumCode() {
    }

    static int value(Enum<?> value) {
        try {
            Object code = value.getClass().getMethod("code").invoke(value);
            if (code instanceof Number) {
                return ((Number) code).intValue();
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
        }
        return value.ordinal();
    }

    static Object enumValue(Object[] values, int code) {
        for (Object value : values) {
            if (value instanceof Enum<?> && value((Enum<?>) value) == code) {
                return value;
            }
        }
        return null;
    }
}
