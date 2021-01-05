package com.github.yfge.fool.dao;

import com.github.yfge.fool.common.annotation.SqlGenerateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Slf4j
public class SqlScriptGenerator {


    private static final String SELECT = "SELECT ";
    private static final String INSERT = "INSERT INTO ";
    private static final String VALUES = " VALUES ";
    private static final String UPDATE = "UPDATE ";
    private static final String FROM = " FROM ";
    private static final String WHERE = "WHERE ";
    private static final String SET = " SET ";
    private static final String COUNT_ONE = " COUNT(1)";

    public String generateSelect(Mapper mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append(SELECT);
        List<MapField> fieldList = mapper.getMapFields();
        builder.append(fieldList.stream().filter(p -> p.isCollection() == false).map(MapField::getColumnName).collect(Collectors.joining(",")));
        builder.append(FROM);
        builder.append("`" + mapper.getTableName() + "`");
        return builder.toString();
    }

    public String generateSelectCount(Mapper mapper) {
        StringBuilder builder = new StringBuilder();
        builder.append(SELECT)
                .append(COUNT_ONE)
                .append(FROM);
        builder.append("`" + mapper.getTableName() + "`");
        return builder.toString();
    }


    public QueryAndArgs generateSelectOne(Mapper mapper, Object key) {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append(SELECT);
        List<MapField> fieldList = mapper.getMapFields();
        builder.append(fieldList.stream().filter(p -> p.isCollection() == false).map(MapField::getColumnName).collect(Collectors.joining(",")));
        builder.append(FROM);
        builder.append("`" + mapper.getTableName() + "`");
        builder.append(WHERE);
        builder.append(mapper.getPrimaryField().getColumnName());
        builder.append(" = ? ");
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(new Object[]{key});
        log.info("generate select one sql:{}", queryAndArgs);
        return queryAndArgs;
    }

    public <T> QueryAndArgs generateUpdate(Mapper<?> mapper, T object) {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append(UPDATE)
                .append('`' + mapper.getTableName() + "`")
                .append(SET);

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
                        if (p == mapper.getPrimaryField()) {
                            filter[0] = " WHERE `" + p.getColumnName() + "`= ?";
                            key[0] = value;
                        }
                        return msg;
                    }).collect(Collectors.joining(",")));

            builder.append(filter[0]);
            objects.add(key[0]);
            builder.append(";");
            queryAndArgs.setSql(builder.toString());
            queryAndArgs.setArgs(objects.toArray());
            log.info("generate update sql : {}", queryAndArgs);
            return queryAndArgs;
        }
        return null;
    }

    public <T> QueryAndArgs generateOnInsert(Mapper<?> mapper, T object) {
        List<Object> objects = new LinkedList<>();
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append(INSERT)
                .append('`' + mapper.getTableName() + "`");

        var fields = mapper.getMapFields().stream().filter(p -> p.isCollection() == false
                && p.getSqlGenerateConfig() != SqlGenerateConfig.AUTO_INCREMENT
                && p.getSqlGenerateConfig() != SqlGenerateConfig.INSERT
                && p.getSqlGenerateConfig() != SqlGenerateConfig.INSERT_AND_UPDATE).collect(Collectors.toList());

        if (fields.size() > 0) {
            builder
                    .append("(")
                    .append(fields.stream().map(p -> "`" + p.getColumnName() + "`").collect(Collectors.joining(",")))
                    .append(")")
                    .append(VALUES)
                    .append("(")
                    .append(fields.stream().map(p -> "?").collect(Collectors.joining(",")))
                    .append(");");
            objects.addAll(fields.stream().map(p -> {
                try {
                    return p.getField().get(object);
                } catch (IllegalAccessException e) {

                }
                return null;
            }).collect(Collectors.toList()));
            queryAndArgs.setSql(builder.toString());
            queryAndArgs.setArgs(objects.toArray());
            return queryAndArgs;
        }
        return null;
    }

    public <T> QueryAndArgs generateAfterInsert(Mapper<?> mapper, T object) {
        List<Object> objects = new LinkedList<>();
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        Optional<MapField> optionalMapField = mapper.getMapFields().stream().filter(p -> p.getSqlGenerateConfig() == SqlGenerateConfig.AUTO_INCREMENT).findFirst();
        if (optionalMapField.isPresent()) {
            builder.append(SELECT)
                    .append(mapper.getMapFields().stream().filter(p -> p.isCollection() == false).map(p -> "`" + p.getColumnName() + "`").collect(Collectors.joining(",")))
                    .append(FROM)
                    .append("`" + mapper.getTableName() + "`")
                    .append(WHERE)
                    .append("`" + optionalMapField.get().getColumnName() + "`=")
                    .append("@@IDENTITY;");

        }
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(objects.toArray());
        return queryAndArgs;
    }
}
