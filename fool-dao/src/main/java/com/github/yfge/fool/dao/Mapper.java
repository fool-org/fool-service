package com.github.yfge.fool.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.yfge.fool.common.annotation.Column;
import com.github.yfge.fool.common.annotation.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Mapper 类，用于实现查询后的值
 *
 * @param <T>
 */
@Data
@Slf4j
public class Mapper<T> extends
        AbstratMapper<T> {
    private static ObjectMapper objectMapper = new ObjectMapper();
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
    private String primaryKeyColumn;
    private PropertyNamingStrategy.PropertyNamingStrategyBase propertyNamingStrategy;

    /**
     * 默认构造函数
     */
    public Mapper(Class<T> clazz) {
        this.clazz = clazz;
        JsonNaming jsonNaming = clazz.getDeclaredAnnotation(JsonNaming.class);
        if (jsonNaming != null) {
            try {
//                log.info("get json naming");
                this.propertyNamingStrategy = (PropertyNamingStrategy.PropertyNamingStrategyBase) jsonNaming.value().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.tableName = getTableName(clazz);
        for (Field field : clazz.getDeclaredFields()
        ) {
            field.setAccessible(true);
            var mapField = new MapField(field, FunctionMap.getFieldFunction(field), getColumnName(field), isCollection(field.getType()));
            mapFields.add(mapField);
            if (mapField.getColumnName().toLowerCase().equals("id")) {
                this.primaryKeyColumn = mapField.getColumnName();
            }
        }
    }

    public boolean isCollection(Class<?> returnType) {
        //判断返回类型是否是集合类型
        boolean isCollection = Collection.class.isAssignableFrom(returnType);
        //判断返回类型是否是数组类型
        boolean isArray = returnType.isArray();
        return isCollection || isArray;
    }

    private String getTableName(Class<T> clazz) {
        Table table = clazz.getDeclaredAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        String className = clazz.getSimpleName();
        if (this.propertyNamingStrategy != null) {
            return this.propertyNamingStrategy.translate(className);
        }

        return className;
    }

    private String getColumnName(Field field) {
        Column column = field.getDeclaredAnnotation(Column.class);
        if (column != null) {
            return column.value();
        }
        String name = field.getName();
        if (this.propertyNamingStrategy != null) {
            name = this.propertyNamingStrategy.translate(name);
        }
        return name;
    }

    @Override
    public T mapRow(ResultSet resultSet, int row) {
        try {
            T t = clazz.getDeclaredConstructor().newInstance();
            for (MapField mapField : mapFields.stream().filter(p -> p.isCollection() == false).collect(Collectors.toList())
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
