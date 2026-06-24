package org.fool.framework.dbmanage;

import java.util.List;
import java.util.Map;

public class SqlResultTable {
    private final List<Map<String, Object>> rows;

    public SqlResultTable(List<Map<String, Object>> rows) {
        this.rows = rows == null ? List.of() : List.copyOf(rows);
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }
}
