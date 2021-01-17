package com.github.yfge.fool.dao;

import lombok.Data;

import java.util.List;


@Data
public class PageResult<T> {
    private List<T> items;
    private PageNavigatorResult pageInfo;

}
