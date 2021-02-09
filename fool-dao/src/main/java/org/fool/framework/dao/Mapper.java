package org.fool.framework.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper 类，用于实现查询后的值
 *
 * @param <T>
 */
@Getter
@Slf4j
public class Mapper<T> extends
        AbstratMapper<T> {
    private static final String ID_DEFAULT = "__ID_DEFAULT";
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
    private Map<String, List<MapField>> groupKeys = new LinkedHashMap<>();
    private MapField primaryField;
    private PropertyNamingStrategy.PropertyNamingStrategyBase propertyNamingStrategy;


    @Autowired


    /**
     * 默认构造函数
     */
    public Mapper(Class<T> clazz) {
        this.clazz = clazz;
        JsonNaming jsonNaming = clazz.getDeclaredAnnotation(JsonNaming.class);
        if (jsonNaming != null) {
            try {
                this.propertyNamingStrategy = (PropertyNamingStrategy.PropertyNamingStrategyBase) jsonNaming.value().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            this.propertyNamingStrategy = (PropertyNamingStrategy.PropertyNamingStrategyBase) PropertyNamingStrategy.SNAKE_CASE;
        }
        this.tableName = getTableName(clazz);
        for (Field field : clazz.getDeclaredFields()
        ) {
            /**
             * 设置成可见
             */
            field.setAccessible(true);
            /**
             * 自动插入和更新相关
             */
            SqlGenerateConfig sqlGenerateConfig = SqlGenerateConfig.NULL;
            SqlGenerate sqlGenerate = field.getDeclaredAnnotation(SqlGenerate.class);
            if (sqlGenerate != null) {
                sqlGenerateConfig = sqlGenerate.value();
            }
            /**
             * mapFiled初始化
             */
            var mapField = new MapField(field, FunctionMap.getFieldFunction(field), getColumnName(field), isCollection(field.getType()), sqlGenerateConfig);
            mapFields.add(mapField);

            /**
             * Id 列
             */
            if (mapField.getSqlGenerateConfig() == SqlGenerateConfig.AUTO_INCREMENT) {
                this.primaryField = mapField;
            } else {
                Id idInfo = field.getDeclaredAnnotation(Id.class);
                if (idInfo != null) {
                    String idGroup = idInfo.value();
                    if (StringUtils.isEmpty(idGroup)) {
                        idGroup = ID_DEFAULT + mapField.getField().getName();
                    }
                    if (!this.groupKeys.containsKey(idGroup)) {
                        this.groupKeys.put(idGroup, new LinkedList<>());
                    }
                    this.groupKeys.get(idGroup).add(mapField);
                }
            }
        }

        /**
         * 如果只有一个，则置为主key
         */
        if (this.primaryField == null && this.groupKeys.keySet().size() == 1) {
            var keys = this.groupKeys.get(this.groupKeys.keySet().toArray()[0]);
            if (keys.size() == 0) {
                this.primaryField = keys.get(0);
                this.groupKeys.clear();
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

    /**
     * @param field
     * @return
     */
    private String getColumnName(Field field) {
        Column column = field.getDeclaredAnnotation(Column.class);
        if (column != null && StringUtils.isEmpty(column.value()) == false) {
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
            T t = null;
            if (this.primaryField != null) {
                String key = resultSet.getString(this.primaryField.getColumnName());
                t = CacheService.getIns().getOrInit(clazz, key);
            } else {
                t = clazz.getDeclaredConstructor().newInstance();
            }
            for (MapField mapField : mapFields.stream().filter(p -> p.isCollection() == false).collect(Collectors.toList())
            ) {
                try {
                    mapField.getField().set(t,
                            mapField.getGetFieldFunction().get(resultSet, mapField.getColumnName()));
                } catch (Exception e) {
                    log.warn("map {} to {} error.", mapField.getColumnName(), mapField.getField().getName(), e);
                }
            }
            return t;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
