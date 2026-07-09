package org.fool.framework.model.service;

import org.fool.framework.common.data.math.MathExpression;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Property;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

public class OperationCommandValueResolver {
    @FunctionalInterface
    public interface BusinessObjectLoader {
        Object load(Property property, String value);
    }

    @FunctionalInterface
    public interface ContextValueLoader {
        Object load(String key);
    }

    public Object resolve(
            Property property,
            IDynamicData data,
            String expression,
            BusinessObjectLoader businessObjectLoader) {
        return resolve(property, data, expression, businessObjectLoader, key -> "");
    }

    public Object resolve(
            Property property,
            IDynamicData data,
            String expression,
            BusinessObjectLoader businessObjectLoader,
            ContextValueLoader contextValueLoader) {
        String value = expression == null ? "" : expression.trim();
        if (isCompositeMathExpression(value)) {
            String result = new MathExpression().calculateParenthesesExpression(
                    value,
                    part -> String.valueOf(resolve(property, data, part, businessObjectLoader, contextValueLoader)));
            return staticValue(property, result, businessObjectLoader);
        }
        if (value.startsWith("$")) {
            return staticValue(property, value.substring(1), businessObjectLoader);
        }
        if (value.startsWith(".")) {
            return value.length() == 1 || data == null ? null : data.get(value.substring(1));
        }
        if (value.startsWith("#.")) {
            if (value.length() == 2 || !(data instanceof DbMysqlDynamic dynamicData) || dynamicData.getOwner() == null) {
                return null;
            }
            return dynamicData.getOwner().get(value.substring(2));
        }
        if (value.startsWith("@")) {
            return contextValue(value.substring(1), contextValueLoader);
        }
        return "";
    }

    private Object contextValue(String expression, ContextValueLoader contextValueLoader) {
        String key = (expression == null ? "" : expression.trim()).toLowerCase(Locale.ROOT);
        return switch (key) {
            case "datetime", "time" -> LocalDateTime.now();
            case "date" -> LocalDateTime.now().toLocalDate().atStartOfDay();
            default -> contextValueLoader == null ? "" : contextValueLoader.load(key);
        };
    }

    private boolean isCompositeMathExpression(String value) {
        if (!MathExpression.isMathExpression(value)) {
            return false;
        }
        if (value.indexOf('+') >= 0 || value.indexOf('*') >= 0 || value.indexOf('/') >= 0
                || value.indexOf('(') >= 0 || value.indexOf(')') >= 0) {
            return true;
        }
        int minusIndex = value.indexOf('-', 1);
        while (minusIndex >= 0 && minusIndex + 1 < value.length()) {
            if ("$.#@".indexOf(value.charAt(minusIndex + 1)) >= 0) {
                return true;
            }
            minusIndex = value.indexOf('-', minusIndex + 1);
        }
        return false;
    }

    private Object staticValue(
            Property property,
            String value,
            BusinessObjectLoader businessObjectLoader) {
        if (property == null || property.getPropertyType() == null) {
            return value;
        }
        return switch (property.getPropertyType()) {
            case Boolean -> Boolean.valueOf(value);
            case Byte -> Byte.valueOf(value);
            case Char -> value.charAt(0);
            case DateTime -> dateTimeValue(value);
            case Int, UInt -> Integer.valueOf(value);
            case Long, ULong, IdentifyId -> Long.valueOf(value);
            case Decimal -> new BigDecimal(value);
            case Double, Float -> Double.valueOf(value);
            case BusinessObject -> businessObjectLoader == null
                    ? value
                    : businessObjectLoader.load(property, value);
            default -> value;
        };
    }

    private LocalDateTime dateTimeValue(String value) {
        String normalized = value.replace(' ', 'T');
        return normalized.length() == 10
                ? LocalDate.parse(normalized).atStartOfDay()
                : LocalDateTime.parse(normalized);
    }
}
