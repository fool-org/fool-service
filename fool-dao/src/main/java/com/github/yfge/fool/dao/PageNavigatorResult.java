package com.github.yfge.fool.dao;


import lombok.Data;

@Data
public class PageNavigatorResult {
    private int pageSize;
    private int pageIndex;
    private long total;
    private long currentPage;
}
