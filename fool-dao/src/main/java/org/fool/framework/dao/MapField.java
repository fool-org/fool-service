package org.fool.framework.dao;


import lombok.Getter;
import org.fool.framework.common.annotation.SqlGenerateConfig;

import java.lang.reflect.Field;

@Getter
public class MapField {
    /**
     * 是否为集合
     */
    private final boolean isCollection;
    /**
     * 字段
     */
    private Field field;
    /**
     * 得到方法
     */
    private GetFieldFunction getFieldFunction;
    /**
     * 列名
     */
    private String columnName;
    /**
     *
     */
    private SqlGenerateConfig sqlGenerateConfig;

    public MapField(Field field, GetFieldFunction getFieldFunction, String colmnName, boolean isCollection, SqlGenerateConfig sqlGenerateConfig) {
        this.field = field;
        this.getFieldFunction = getFieldFunction;
        this.columnName = colmnName;
        this.isCollection = isCollection;
        this.sqlGenerateConfig = sqlGenerateConfig;
    }
}
