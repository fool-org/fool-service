package com.github.yfge.fool.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class SqlScriptGenerator {

    public String generateSelect(Mapper mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        List<MapField> fieldList = mapper.getMapFields();
        builder.append(fieldList.stream().filter(p -> p.isCollection() == false).map(MapField::getColumnName).collect(Collectors.joining(",")));
        builder.append(" FROM ");
        builder.append("`" + mapper.getTableName() + "`");
        return builder.toString();
    }

    public String generateSelectCount(Mapper mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT  count(1) ");
        builder.append(" FROM ");
        builder.append("`" + mapper.getTableName() + "`");
        return builder.toString();
    }


    public QueryAndArgs generateSelectOne(Mapper mapper, Object key) {

        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ");
        List<MapField> fieldList = mapper.getMapFields();
        builder.append(fieldList.stream().filter(p -> p.isCollection() == false).map(MapField::getColumnName).collect(Collectors.joining(",")));
        builder.append(" FROM ");
        builder.append("`" + mapper.getTableName() + "`");
        builder.append(" where ");
        builder.append(mapper.getPrimaryKeyColumn());
        builder.append(" = ? ");
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(new Object[]{key});
        return queryAndArgs;

    }

    public <T> QueryAndArgs generateUpdate(Mapper<?> mapper, T object) {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ");
        builder.append('`' + mapper.getTableName() + "` SET ");
        final Object[] key = new Object[1];
        final String[] filter = {""};
        var fields =
                mapper.getMapFields().stream().filter(p -> p.isCollection() == false).collect(Collectors.toList());
        if (fields.size() > 0) {
            List<Object> objects = new LinkedList<>();
            builder.append(
                    fields.stream().map(p -> {
                        Object value = null;
                        try {
                            value = p.getField().get(object);

                        } catch (IllegalAccessException e) {
                            log.error("{}", e);
                        }
                        objects.add(value);
                        String msg = "`" + p.getColumnName() + "` = ?";
                        if (p.getColumnName().equals(mapper.getPrimaryKeyColumn())) {
                            filter[0] = " WHERE `" + mapper.getPrimaryKeyColumn() + "`= ?";
                            key[0] = value;
                        }
                        return msg;
                    }).collect(Collectors.joining(",")));

            builder.append(filter[0]);
            objects.add(key[0]);
            queryAndArgs.setSql(builder.toString());
            queryAndArgs.setArgs(objects.toArray());
            log.info("{}", queryAndArgs);
            return queryAndArgs;
        }
        return null;
    }
}
