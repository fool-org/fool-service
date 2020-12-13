package com.github.yfge.fool.dao;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class SqlScriptGenerator {

    public String generateSelect(Mapper mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        List<MapField> fieldList = mapper.getMapFields();
        builder.append(fieldList.stream().map(MapField::getColumnName).collect(Collectors.joining(",")));
        builder.append(" FROM ");
        builder.append("`" + mapper.getTableName() + "`");
        return builder.toString();
    }

    public String generateCount(Mapper mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT  count(1) ");
        builder.append(" FROM ");
        builder.append("`" + mapper.getTableName() + "`");
        return builder.toString();
    }


}
