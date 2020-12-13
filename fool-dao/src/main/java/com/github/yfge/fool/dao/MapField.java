package com.github.yfge.fool.dao;



import lombok.Data;

import java.lang.reflect.Field;

@Data
public class MapField {
    private Field field;
    private GetFieldFunction getFieldFunction;
    private String columnName;

    public MapField(Field field, GetFieldFunction getFieldFunction, String colmnName) {
        this.field = field;
        this.getFieldFunction = getFieldFunction;
        this.columnName = colmnName;
    }
}
