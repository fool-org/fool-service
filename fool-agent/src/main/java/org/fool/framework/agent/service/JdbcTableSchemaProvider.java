package org.fool.framework.agent.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;

public class JdbcTableSchemaProvider implements TableSchemaProvider {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTableSchemaProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TableSchemaSnapshot load(String tableName) {
        if (!StringUtils.hasText(tableName)) {
            return TableSchemaSnapshot.unavailable("Model table name is required.");
        }
        try {
            List<String> columns = jdbcTemplate.queryForList("""
                    SELECT COLUMN_NAME
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                    ORDER BY ORDINAL_POSITION
                    """, String.class, tableName.trim());
            return columns.isEmpty()
                    ? TableSchemaSnapshot.unavailable("Target table was not found in the current database.")
                    : TableSchemaSnapshot.hydrated(new LinkedHashSet<>(columns));
        } catch (DataAccessException ex) {
            return TableSchemaSnapshot.unavailable("Failed to read target table schema.");
        }
    }
}
