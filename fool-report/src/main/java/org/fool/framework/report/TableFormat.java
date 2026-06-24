package org.fool.framework.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableFormat {
    private String sourceTable;
    private List<CellFormat> colums = new ArrayList<>();
    private List<CellFormat> rows = new ArrayList<>();
    private List<ValueCell> valueCell = new ArrayList<>();
}
