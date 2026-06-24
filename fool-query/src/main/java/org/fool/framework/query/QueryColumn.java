package org.fool.framework.query;

import lombok.Data;
import org.fool.framework.common.PropertyType;

@Data
public class QueryColumn {
    private QueryTable table;
    private PropertyType dataType;
    private String formatStr;
    private boolean identity;
    private boolean key;
    private String showName;
    private String dbName;
    private String id;
}
