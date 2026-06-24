package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompareCol {
    private QueryColumn col;
    private String selectedTableName;

    public CompareCol(QueryColumn col, String selectedTableName) {
        this.col = col;
        this.selectedTableName = selectedTableName;
    }
}
