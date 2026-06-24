package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QueryTable {
    private String showName;
    private String dbName;

    public QueryTable(String showName, String dbName) {
        this.showName = showName;
        this.dbName = dbName;
    }
}
