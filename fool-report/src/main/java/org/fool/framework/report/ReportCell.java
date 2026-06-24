package org.fool.framework.report;

import lombok.Data;

@Data
public class ReportCell {
    private int col;
    private int row;
    private int colSpan;
    private int rowSpan;
    private String fmtValue;
}
