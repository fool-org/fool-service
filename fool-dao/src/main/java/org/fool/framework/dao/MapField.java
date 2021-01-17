package org.fool.framework.dao;


import org.fool.framework.common.annotation.SqlGenerateConfig;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
public class MapField {
    private final boolean isCollection;
    private Field field;
    private GetFieldFunction getFieldFunction;
    private String columnName;
    private SqlGenerateConfig sqlGenerateConfig;

    public MapField(Field field, GetFieldFunction getFieldFunction, String colmnName, boolean isCollection, SqlGenerateConfig sqlGenerateConfig) {
        this.field = field;
        this.getFieldFunction = getFieldFunction;
        this.columnName = colmnName;
        this.isCollection = isCollection;
        this.sqlGenerateConfig = sqlGenerateConfig;
    }
}
