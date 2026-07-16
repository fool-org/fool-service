package org.fool.framework.model.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class NonDestructiveDdlActionHandler implements ControlledActionHandler {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public NonDestructiveDdlActionHandler(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String action() { return "model.ddl.execute"; }
    public String resourceType() { return "Model"; }
    public void preflight(ControlledActionContext context) { plan(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return schema(context).version(); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        Plan plan = plan(context);
        Schema schema = schema(context);
        return new ControlledActionPreview(schema.version(), plan.columns().size(),
                Map.of("table", plan.table(), "addColumns", plan.columns().stream()
                        .map(column -> Map.of("name", column.name(), "type", column.sqlType(), "nullable", true))
                        .toList(), "generatedStatements", plan.statements()),
                List.of("target table and column set match the approved schema snapshot",
                        "all additions are nullable and use an allowlisted type"),
                "DDL can implicitly commit; if recovery is required, manually review and drop only the listed new columns",
                List.of("automatic transaction rollback is not claimed", "take a database backup before execution"),
                List.of("DDL_CHANGE", "NON_ROLLBACKABLE"));
    }

    public ControlledActionResult execute(ControlledActionContext context) {
        Plan plan = plan(context);
        requireApprovedSnapshot(context, schema(context).version());
        for (String statement : plan.statements()) {
            jdbcTemplate.execute(statement);
        }
        Schema after = schema(context);
        return new ControlledActionResult("DDL_EXECUTED",
                Map.of("table", plan.table(), "addedColumns", plan.columns().stream().map(ColumnPlan::name).toList(),
                        "schemaVersionAfter", after.version()),
                Map.of("manualRecovery", "drop only the approved added columns after dependency review"));
    }

    private Plan plan(ControlledActionContext context) {
        String table = jdbcTemplate.query("""
                SELECT `table_name` FROM `fool_sys_model` WHERE `name` = ? LIMIT 1
                """, rs -> rs.next() ? rs.getString(1) : null, context.resourceId());
        if (table == null || !table.matches("[A-Za-z0-9_]{1,64}")) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        Object value = context.arguments().get("addColumns");
        if (!(value instanceof List<?> requested) || requested.isEmpty() || requested.size() > 10) {
            throw denied("DDL_COLUMNS_INVALID");
        }
        Set<String> existing = Set.copyOf(jdbcTemplate.queryForList("""
                SELECT `COLUMN_NAME` FROM information_schema.`COLUMNS`
                 WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ?
                """, String.class, table));
        List<ColumnPlan> columns = new ArrayList<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (Object item : requested) {
            if (!(item instanceof Map<?, ?> map)) throw denied("DDL_COLUMNS_INVALID");
            String name = text(map.get("name"));
            if (!name.matches("[A-Za-z][A-Za-z0-9_]{0,63}") || !seen.add(name) || existing.contains(name)) {
                throw denied(existing.contains(name) ? "DDL_COLUMN_ALREADY_EXISTS" : "DDL_COLUMNS_INVALID");
            }
            if (Boolean.FALSE.equals(map.get("nullable"))) throw denied("DESTRUCTIVE_DDL");
            String sqlType = sqlType(map);
            columns.add(new ColumnPlan(name, sqlType));
        }
        List<String> statements = columns.stream()
                .map(column -> "ALTER TABLE `" + table + "` ADD COLUMN `" + column.name() + "` "
                        + column.sqlType() + " NULL")
                .toList();
        return new Plan(table, List.copyOf(columns), statements);
    }

    private static String sqlType(Map<?, ?> map) {
        String type = text(map.get("type")).toUpperCase(Locale.ROOT);
        return switch (type) {
            case "VARCHAR" -> "VARCHAR(" + integer(map.get("length"), 1, 1024, "DDL_TYPE_INVALID") + ")";
            case "DECIMAL" -> {
                int precision = integer(map.get("precision"), 1, 38, "DDL_TYPE_INVALID");
                int scale = integer(map.get("scale"), 0, precision, "DDL_TYPE_INVALID");
                yield "DECIMAL(" + precision + "," + scale + ")";
            }
            case "BIGINT", "INT", "DATETIME", "TEXT" -> type;
            case "BOOLEAN" -> "TINYINT(1)";
            default -> throw denied("DDL_TYPE_INVALID");
        };
    }

    private Schema schema(ControlledActionContext context) {
        String table = jdbcTemplate.query("""
                SELECT `table_name` FROM `fool_sys_model` WHERE `name` = ? LIMIT 1
                """, rs -> rs.next() ? rs.getString(1) : null, context.resourceId());
        if (table == null || !table.matches("[A-Za-z0-9_]{1,64}")) throw denied("RESOURCE_OUT_OF_SCOPE");
        List<Map<String, Object>> columns = jdbcTemplate.query("""
                SELECT `COLUMN_NAME`, `COLUMN_TYPE`, `IS_NULLABLE`, `COLUMN_DEFAULT`, `EXTRA`
                  FROM information_schema.`COLUMNS`
                 WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = ? ORDER BY `ORDINAL_POSITION`
                """, (rs, rowNum) -> {
            Map<String, Object> column = new LinkedHashMap<>();
            column.put("name", rs.getString(1));
            column.put("type", rs.getString(2));
            column.put("nullable", rs.getString(3));
            column.put("default", rs.getString(4));
            column.put("extra", rs.getString(5));
            return column;
        }, table);
        if (columns.isEmpty()) throw denied("RESOURCE_OUT_OF_SCOPE");
        return new Schema(hash(columns), columns);
    }

    private void requireApprovedSnapshot(ControlledActionContext context, String current) {
        Object expected = context.arguments().get("_approvedSnapshotVersion");
        if (!(expected instanceof String value) || !value.equals(current)) {
            throw new ControlledActionException(409, "OBJECT_CHANGED");
        }
    }

    private String hash(Object value) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(value);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        } catch (Exception ex) {
            throw denied("DDL_SCHEMA_INVALID");
        }
    }

    private static int integer(Object value, int min, int max, String reason) {
        int result;
        try { result = value instanceof Number number ? number.intValue() : Integer.parseInt(text(value)); }
        catch (RuntimeException ex) { throw denied(reason); }
        if (result < min || result > max) throw denied(reason);
        return result;
    }

    private static String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private static ControlledActionException denied(String reason) { return new ControlledActionException(400, reason); }

    private record ColumnPlan(String name, String sqlType) {}
    private record Plan(String table, List<ColumnPlan> columns, List<String> statements) {}
    private record Schema(String version, List<Map<String, Object>> columns) {}
}
