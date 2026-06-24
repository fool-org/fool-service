package org.fool.framework.query;

import lombok.Data;
import org.fool.framework.common.PropertyType;

@Data
public class LegacyCompareOp {
    private long id;
    private String showName;
    private String dbName;
    private PropertyType propertyType;
}
