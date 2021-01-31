package org.fool.framework.dao;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 得到枚举的操作
 */
@Slf4j
public class FunctionMap {

    private static ConcurrentHashMap<String, GetFieldFunction> map = new ConcurrentHashMap<>();

    /**
     *
     */
    //todo 处理基本类型
    static {
        map.put("java.lang.Integer", ResultSet::getInt);
        map.put("java.lang.Double", ResultSet::getDouble);
        map.put("java.lang.Float", ResultSet::getFloat);
        map.put("java.lang.Long", ResultSet::getLong);
        map.put("java.lang.Short", ResultSet::getShort);
        map.put("java.lang.Byte", ResultSet::getByte);
        map.put("java.lang.Boolean", ResultSet::getBoolean);
        map.put("java.lang.Character", ResultSet::getByte);
        map.put("java.lang.String", ResultSet::getString);
        map.put("int", ResultSet::getInt);
        map.put("double", ResultSet::getDouble);
        map.put("long", ResultSet::getLong);
        map.put("short", ResultSet::getShort);
        map.put("byte", ResultSet::getByte);
        map.put("boolean", ResultSet::getBoolean);
        map.put("char", ResultSet::getByte);
        map.put("float", ResultSet::getFloat);
        map.put("java.math.BigDecimal", ResultSet::getBigDecimal);
        map.put("java.time.LocalDateTime", new GetFieldFunction() {
            @Override
            public Object get(ResultSet resultSet, String columnName) throws SQLException {
                return LocalDateTime.now();
            }
        });
    }

    /**
     * 得到get操作
     *
     * @param field
     * @return
     */
    public static GetFieldFunction getFieldFunction(Field field) {
        String name = field.getType().getName();
        if (field.getType().isEnum()) {
            return new GetFieldFunction() {
                @Override
                public Object get(ResultSet resultSet, String columnName) throws SQLException {
                    var values = field.getType().getEnumConstants();
                    Integer index = resultSet.getInt(columnName);
                    if (index != null && index < values.length) {
                        return values[index];
                    }
                    return null;
                }
            };
        }
        return map.getOrDefault(name, null);
    }
}
