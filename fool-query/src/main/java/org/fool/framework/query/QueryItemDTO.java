package org.fool.framework.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class QueryItemDTO {
    private String display;
    private String dbValue;

    public QueryItemDTO(String name, String value) {
        this.display = name;
        this.dbValue = value;
    }
}
