package org.fool.framework.dao;

import lombok.Data;

import java.util.List;


@Data
public class PageResult<T> {
    private List<T> items;
    private PageNavigatorResult pageInfo;

}
