package org.fool.framework.query;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class QueryColumnCollection extends ArrayList<QueryColumn> {

    public QueryColumn find(String expression) {
        String normalizedExpression = normalize(expression);
        for (QueryColumn column : this) {
            if (matches(column, normalizedExpression)) {
                return column;
            }
        }
        return null;
    }

    public QueryColumn get(String expression) {
        return find(expression);
    }

    private boolean matches(QueryColumn column, String normalizedExpression) {
        return Objects.equals(normalize(column.getDbName()), normalizedExpression)
                || Objects.equals(normalize(column.getShowName()), normalizedExpression)
                || Objects.equals(normalize(column.getTable().getDbName()) + "." + normalize(column.getDbName()),
                        normalizedExpression)
                || Objects.equals(normalize(column.getTable().getShowName()) + "." + normalize(column.getShowName()),
                        normalizedExpression);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
