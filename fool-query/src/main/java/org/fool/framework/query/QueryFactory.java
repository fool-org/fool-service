package org.fool.framework.query;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@FunctionalInterface
public interface QueryFactory {
    List<JoinTable> getCanJoinedTables(QueryTable table, JoinQueryType joinType);

    default List<QueryTable> getTables() {
        return List.of();
    }

    default QueryTable getTable(String tableName) {
        String normalized = normalize(tableName);
        return getTables().stream()
                .filter(table -> Objects.equals(normalize(table.getShowName()), normalized)
                        || Objects.equals(normalize(table.getDbName()), normalized))
                .findFirst()
                .orElse(null);
    }

    default List<QueryColumn> getColumns(QueryTable table) {
        return List.of();
    }

    default List<ColStateValue> getStateValues(QueryColumn col) {
        return List.of();
    }

    default String getStateStr(QueryColumn col, String value) {
        return getStateValues(col).stream()
                .filter(stateValue -> Objects.equals(stateValue.getDbName(), value))
                .map(ColStateValue::getShowName)
                .findFirst()
                .orElse("");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
