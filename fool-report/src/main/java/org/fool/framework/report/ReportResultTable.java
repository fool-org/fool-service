package org.fool.framework.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportResultTable {
    private String name;
    private List<ReportResultTableColumn> columns;
}
