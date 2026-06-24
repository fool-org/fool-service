package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Repository
public class JdbcEventModelTableResolver implements EventModelTableResolver {
    static final String DEFAULT_OBJECT_ID_COLUMN = "ID";
    static final String TABLE_NAME_COLUMN = "table_name";
    static final String OBJECT_ID_COLUMN = "object_id_column";
    static final String SELECT_MODEL_TABLE_SQL = """
            SELECT model.`id`, model.`name`, model.`table_name`, property.`column` AS `object_id_column`
            FROM `fool_sys_model` model
            LEFT JOIN `fool_sys_model_property` property
              ON model.`id_property` = property.`id`
            WHERE CAST(model.`id` AS CHAR) = ? OR model.`name` = ?
            ORDER BY CASE WHEN CAST(model.`id` AS CHAR) = ? THEN 0 ELSE 1 END
            LIMIT 1
            """;

    private final Function<String, List<Map<String, Object>>> modelRows;

    @Autowired
    public JdbcEventModelTableResolver(JdbcTemplate jdbcTemplate) {
        this(modelId -> jdbcTemplate.queryForList(SELECT_MODEL_TABLE_SQL, modelId, modelId, modelId));
    }

    JdbcEventModelTableResolver(Function<String, List<Map<String, Object>>> modelRows) {
        this.modelRows = modelRows;
    }

    @Override
    public EventModelQueryMetadata resolve(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return new EventModelQueryMetadata(modelId, DEFAULT_OBJECT_ID_COLUMN);
        }
        return modelRows.apply(modelId).stream()
                .map(row -> new EventModelQueryMetadata(
                        valueOrDefault(row.get(TABLE_NAME_COLUMN), modelId),
                        valueOrDefault(row.get(OBJECT_ID_COLUMN), DEFAULT_OBJECT_ID_COLUMN)))
                .findFirst()
                .orElse(new EventModelQueryMetadata(modelId, DEFAULT_OBJECT_ID_COLUMN));
    }

    private static String valueOrDefault(Object value, String defaultValue) {
        String stringValue = valueOf(value);
        return stringValue == null || stringValue.isBlank() ? defaultValue : stringValue;
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }
}
