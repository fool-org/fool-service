package org.fool.framework.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public final class PropertyTypeAdaper {
    private static final LocalDateTime LEGACY_MIN_DATE_TIME = LocalDateTime.of(1, 1, 1, 0, 0);

    private PropertyTypeAdaper() {
    }

    public static PropertyType getPropertyType(Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return PropertyType.Int;
        }
        if (type == long.class || type == Long.class) {
            return PropertyType.Long;
        }
        if (type == boolean.class || type == Boolean.class) {
            return PropertyType.Boolean;
        }
        if (type == BigDecimal.class) {
            return PropertyType.Decimal;
        }
        if (type == char.class || type == Character.class) {
            return PropertyType.Char;
        }
        if (type == byte.class || type == Byte.class) {
            return PropertyType.Byte;
        }
        if (type == String.class) {
            return PropertyType.String;
        }
        if (type == LocalDate.class) {
            return PropertyType.Date;
        }
        if (type == LocalTime.class) {
            return PropertyType.Time;
        }
        if (type == LocalDateTime.class) {
            return PropertyType.DateTime;
        }
        if (type == float.class || type == Float.class) {
            return PropertyType.Float;
        }
        if (type == double.class || type == Double.class) {
            return PropertyType.Double;
        }
        if (type == UUID.class) {
            return PropertyType.Guid;
        }
        if (type.isEnum()) {
            return PropertyType.Enum;
        }
        return PropertyType.BusinessObject;
    }

    public static Object getDefaultValue(PropertyType propertyType) {
        return switch (propertyType) {
            case Boolean -> false;
            case Byte -> (byte) 0;
            case Char -> '\0';
            case Date, DateTime, Time -> LEGACY_MIN_DATE_TIME;
            case Decimal -> BigDecimal.ZERO;
            case Double -> 0D;
            case Enum, Int -> 0;
            case Float -> 0F;
            case Long, IdentifyId -> 0L;
            case String -> "";
            case UInt -> 0;
            case ULong -> 0L;
            case Guid -> new UUID(0L, 0L);
            default -> null;
        };
    }
}
