package org.fool.framework.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiDbMap {
    private String propertyName;
    private String columnName;
}
