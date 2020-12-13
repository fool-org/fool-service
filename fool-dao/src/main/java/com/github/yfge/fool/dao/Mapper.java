package com.github.yfge.fool.dao;

import com.github.yfge.fool.common.annotation.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;


/**
 * Mapper 类，用于实现查询后的值
 *
 * @param <T>
 */
@Data
@Slf4j
public class Mapper<T> extends
        AbstratMapper<T> {
    /**
     * 类的信息
     */
    private Class<T> clazz;
    /**
     * 表的信息
     */
    private String tableName;
    /**
     * 映射的信息
     */
    private List<MapField> mapFields = new LinkedList<>();


    /**
     * 默认构造函数
     */
    public Mapper(Class<T> clazz) {
        this.clazz = clazz;
        this.tableName = getTableName(clazz);
        for (Field field : clazz.getDeclaredFields()
        ) {
            field.setAccessible(true);
            mapFields.add(new MapField(field, FunctionMap.getFieldFunction(field), getColumnName(field)));
        }
    }

    private String getTableName(Class<T> clazz) {
        Table table = clazz.getDeclaredAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return
                clazz.getSimpleName();
    }

    private String getColumnName(Field field) {
        return field.getName();
    }

    @Override
    public T mapRow(ResultSet resultSet, int row) {
        try {
            T t = clazz.getDeclaredConstructor().newInstance();
            for (MapField mapField : mapFields
            ) {
                mapField.getField().set(t,
                        mapField.getGetFieldFunction().get(resultSet, mapField.getColumnName()));
            }
            return t;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
