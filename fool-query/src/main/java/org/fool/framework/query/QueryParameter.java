package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QueryParameter {
    private CompareCol column;
    private String name;
    private Object value;

    public QueryParameter(String name, CompareCol column, Object value) {
        this.name = name;
        this.column = column;
        this.value = value;
    }
}
