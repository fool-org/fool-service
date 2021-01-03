package com.github.yfge.fool.dao;


import lombok.Data;

@Data
public class QueryAndArgs {
    private String sql;
    private Object[] args;
}
