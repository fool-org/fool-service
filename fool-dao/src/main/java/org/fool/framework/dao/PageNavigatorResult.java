package org.fool.framework.dao;


import lombok.Data;

@Data
public class PageNavigatorResult {
    private int pageSize;
    private long pageIndex;
    private long total;
    private long pageCount;
}
