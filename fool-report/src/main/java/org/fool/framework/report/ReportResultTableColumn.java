package org.fool.framework.report;

import lombok.Data;
import org.fool.framework.common.PropertyType;

@Data
public class ReportResultTableColumn {
    private String colName;
    private PropertyType dataType;
    private int index;
}
