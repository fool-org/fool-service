package org.fool.framework.event;

public final class EventSqlHelper {
    private EventSqlHelper() {
    }

    public static String buildQuerySql(String tableName, EventDefinition definition) {
        String filter = definition.getFilter() == null ? "" : definition.getFilter();
        if (tableName != null && !tableName.trim().startsWith("[")) {
            filter = mysqlBrackets(filter);
        }
        return String.format("SELECT * FROM %s WHERE %s", tableName, filter);
    }

    private static String mysqlBrackets(String filter) {
        return filter.replaceAll("\\[([A-Za-z_][A-Za-z0-9_]*)]", "`$1`");
    }
}
