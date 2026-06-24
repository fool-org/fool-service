package org.fool.framework.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportGridResult {
    private int viewId;
    private int currentPage;
    private int pageSize;
    private long totalRecords;
    private long totalPages;
    private List<ReportCell> cells = new ArrayList<>();
}
