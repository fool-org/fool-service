package org.fool.framework.query;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class QueryResult {
    private long totalPages;
    @Setter
    private int currentPage;
    private long totalRecords;
    private final int pageSize;
    private List<Map<String, Object>> rows = new ArrayList<>();

    public QueryResult(int pageSize) {
        this.pageSize = pageSize;
        this.currentPage = 1;
    }

    public void updatePage(long totalRecords, List<Map<String, Object>> rows) {
        this.totalRecords = totalRecords;
        this.totalPages = totalRecords / pageSize + (totalRecords % pageSize == 0 ? 0 : 1);
        this.rows = new ArrayList<>(rows);
    }
}
