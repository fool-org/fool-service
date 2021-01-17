package org.fool.framework.dao;

import org.fool.framework.common.annotation.SqlGenerateConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
@Slf4j
public class SqlScriptGenerator {


    private static final String SELECT = "SELECT ";
    private static final String INSERT = "INSERT INTO ";
    private static final String DELETE = "DELETE FROM ";
    private static final String VALUES = " VALUES ";
    private static final String UPDATE = "UPDATE ";
    private static final String FROM = " FROM ";
    private static final String WHERE = "WHERE 1=1 ";
    private static final String SET = " SET ";
    private static final String COUNT_ONE = " COUNT(1)";
    private static final String AND = " AND ";
    private static final String PAGE_INFO = " LIMIT ? OFFSET ?";

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
        builder.append(AND);
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
                    .append(AND)
                    .append("`" + optionalMapField.get().getColumnName() + "`=")
                    .append("@@IDENTITY;");

        } else {
            return null;
        }
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(objects.toArray());
        return queryAndArgs;
    }

    public <T> QueryAndArgs generateDelete(Mapper<?> mapper, T object) {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append(DELETE)
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

    public QueryAndArgs generateSelectithPageBySimpleFilter(Mapper<?> mapper, PageNavigator pageNavigator, Map<String, Object[]> filter) {
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        StringBuilder builder = new StringBuilder();
        builder.append(SELECT);
        List<MapField> fieldList = mapper.getMapFields();
        builder.append(fieldList.stream().filter(p -> p.isCollection() == false).map(MapField::getColumnName).collect(Collectors.joining(",")));
        builder.append(FROM);
        builder.append("`" + mapper.getTableName() + "`");
        builder.append(WHERE);

        List<Object> params = new LinkedList<>();

        if (!CollectionUtils.isEmpty(filter)) {
            for (String key : filter.keySet()
            ) {
                var field = fieldList.stream().filter(p -> p.isCollection() == false && (p.getColumnName().equals(key) || p.getField().getName().equals(key))).findFirst();
                if (field.isPresent()) {
                    var values = filter.get(key);
                    builder.append(AND);
                    if (values.length == 1) {
                        builder.append("`" + field.get().getColumnName() + "` = ? ");
                        params.add(values[0]);

                    } else if (values.length == 2) {
                        builder.append("`" + field.get().getColumnName() + "` BETWEEN ? AND ? ");
                        params.add(values[0]);
                        params.add(values[1]);
                    }
                }
            }
        }

        builder.append(PAGE_INFO);
        params.add(pageNavigator.getPageSize());
        params.add(pageNavigator.getPageSize() * (pageNavigator.getPageIndex() - 1));

        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(params.toArray());
        log.info("generate select one sql:{}", queryAndArgs);

        return queryAndArgs;
    }
}
