package org.fool.framework.query;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class QueryResult {
    private long totalPages;
    private int currentPage;
    private long totalRecords;
    private final int pageSize;
    private final PageLoader pageLoader;
    private boolean pageDirty;
    private List<Map<String, Object>> rows = new ArrayList<>();

    public QueryResult(int pageSize) {
        this(pageSize, null);
    }

    QueryResult(int pageSize, PageLoader pageLoader) {
        this.pageSize = pageSize;
        this.currentPage = 1;
        this.pageLoader = pageLoader;
    }

    public void updatePage(long totalRecords, List<Map<String, Object>> rows) {
        this.totalRecords = totalRecords;
        this.totalPages = totalRecords / pageSize + (totalRecords % pageSize == 0 ? 0 : 1);
        this.rows = new ArrayList<>(rows);
        this.pageDirty = false;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        this.pageDirty = pageLoader != null;
    }

    public List<Map<String, Object>> getData() {
        if (pageDirty) {
            Page page = pageLoader.load(pageSize, currentPage);
            updatePage(page.totalRecords(), page.rows());
        }
        return rows;
    }

    @FunctionalInterface
    interface PageLoader {
        Page load(int pageSize, int currentPage);
    }

    record Page(long totalRecords, List<Map<String, Object>> rows) {
    }
}
