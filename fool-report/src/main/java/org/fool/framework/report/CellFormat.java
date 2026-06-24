package org.fool.framework.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CellFormat {
    private String sourceColumn;
    private String colName;
    private String format;
    private String orderColumn;
    private OrderType orderType;
    private List<StaticFormat> staticFormats = new ArrayList<>();
}
