package org.fool.framework.event;

public final class EventSqlHelper {
    private EventSqlHelper() {
    }

    public static String buildQuerySql(String tableName, EventDefinition definition) {
        return String.format("SELECT * FROM %s WHERE %s", tableName, definition.getFilter());
    }
}
