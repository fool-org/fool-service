package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcEventObjectQuery implements EventObjectQuery {
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
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new EventMatchedObject(rs.getString(metadata.objectIdColumn()), rowValues(rs)));
    }

    private static Map<String, Object> rowValues(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, Object> values = new LinkedHashMap<>();
        for (int index = 1; index <= metaData.getColumnCount(); index++) {
            values.put(columnLabel(metaData, index), resultSet.getObject(index));
        }
        return values;
    }

    private static String columnLabel(ResultSetMetaData metaData, int index) throws SQLException {
        String label = metaData.getColumnLabel(index);
        return label == null || label.isBlank() ? metaData.getColumnName(index) : label;
    }
}
