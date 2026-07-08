package org.fool.framework.report;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("ViewId")
    public int getLegacyViewId() {
        return viewId;
    }

    @JsonProperty("CurrentPage")
    public int getLegacyCurrentPage() {
        return currentPage;
    }

    @JsonProperty("PageSize")
    public int getLegacyPageSize() {
        return pageSize;
    }

    @JsonProperty("TotalRecords")
    public long getLegacyTotalRecords() {
        return totalRecords;
    }

    @JsonProperty("TotalPages")
    public long getLegacyTotalPages() {
        return totalPages;
    }

    @JsonProperty("Cells")
    public List<ReportCell> getLegacyCells() {
        return cells;
    }
}
