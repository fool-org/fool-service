package org.fool.framework.query;

import java.util.List;
import java.util.Objects;

@FunctionalInterface
public interface QueryFactory {
    List<JoinTable> getCanJoinedTables(QueryTable table, JoinQueryType joinType);

    default List<QueryTable> getTables() {
        return List.of();
    }

    default QueryTable getTable(String tableName) {
        return getTables().stream()
                .filter(table -> Objects.equals(table.getShowName(), tableName)
                        || Objects.equals(table.getDbName(), tableName))
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
                .filter(stateValue -> Objects.equals(stateValue.getShowName(), value))
                .map(ColStateValue::getDbName)
                .findFirst()
                .orElse(value);
    }
}
