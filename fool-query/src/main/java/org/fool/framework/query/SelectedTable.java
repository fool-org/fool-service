package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SelectedTable {
    private QueryTable table;
    private String selectedTableName;

    public SelectedTable(QueryTable table, String selectedTableName) {
        this.table = table;
        this.selectedTableName = selectedTableName;
    }
}
