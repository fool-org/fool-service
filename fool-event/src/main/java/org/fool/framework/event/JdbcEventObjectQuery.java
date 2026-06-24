package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcEventObjectQuery implements EventObjectQuery {
    static final String MISSING_ID_COLUMN_MESSAGE = "Can't Gerneration Query Because The Id Column Isn't Included!";

    private final JdbcTemplate jdbcTemplate;
    private final EventModelTableResolver tableResolver;

    @Autowired
    public JdbcEventObjectQuery(JdbcTemplate jdbcTemplate, EventModelTableResolver tableResolver) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableResolver = tableResolver;
    }

    @Override
    public List<EventMatchedObject> findMatchedObjects(EventDefinition definition) {
        if (definition.getModelId() == null || definition.getModelId().isBlank()) {
            return List.of();
        }
        EventModelQueryMetadata metadata = tableResolver.resolve(definition.getModelId());
        String sql = EventSqlHelper.buildQuerySql(metadata.tableName(), definition);
        ResultSetExtractor<List<EventMatchedObject>> extractor =
                resultSet -> matchedObjects(resultSet, metadata.objectIdColumn());
        return jdbcTemplate.query(sql, extractor);
    }

    private static List<EventMatchedObject> matchedObjects(ResultSet resultSet, String objectIdColumn)
            throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<String> columns = columnLabels(metaData);
        String matchedObjectIdColumn = requireObjectIdColumn(columns, objectIdColumn);
        List<EventMatchedObject> matchedObjects = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> values = rowValues(resultSet, columns);
            Object objectId = values.get(matchedObjectIdColumn);
            matchedObjects.add(new EventMatchedObject(objectId == null ? null : objectId.toString(), values));
        }
        return matchedObjects;
    }

    private static List<String> columnLabels(ResultSetMetaData metaData) throws SQLException {
        List<String> columns = new ArrayList<>();
        for (int index = 1; index <= metaData.getColumnCount(); index++) {
            columns.add(columnLabel(metaData, index));
        }
        return columns;
    }

    private static String requireObjectIdColumn(List<String> columns, String objectIdColumn) {
        for (String column : columns) {
            if (column.equals(objectIdColumn)) {
                return column;
            }
        }
        for (String column : columns) {
            if (column.equalsIgnoreCase(objectIdColumn)) {
                return column;
            }
        }
        throw new IllegalStateException(MISSING_ID_COLUMN_MESSAGE);
    }

    private static Map<String, Object> rowValues(ResultSet resultSet, List<String> columns) throws SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int index = 1; index <= columns.size(); index++) {
            values.put(columns.get(index - 1), resultSet.getObject(index));
        }
        return values;
    }

    private static String columnLabel(ResultSetMetaData metaData, int index) throws SQLException {
        String label = metaData.getColumnLabel(index);
        return label == null || label.isBlank() ? metaData.getColumnName(index) : label;
    }
}
