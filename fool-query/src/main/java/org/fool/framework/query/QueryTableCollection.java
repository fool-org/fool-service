package org.fool.framework.query;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class QueryTableCollection extends ArrayList<QueryTable> {

    public QueryTable find(String expression) {
        String normalizedExpression = normalize(expression);
        for (QueryTable table : this) {
            if (Objects.equals(normalize(table.getDbName()), normalizedExpression)
                    || Objects.equals(normalize(table.getShowName()), normalizedExpression)) {
                return table;
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
