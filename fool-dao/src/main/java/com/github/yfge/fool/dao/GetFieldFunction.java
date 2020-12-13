package com.github.yfge.fool.dao;


import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface GetFieldFunction<T> {
    T get(ResultSet resultSet, String columnName) throws SQLException;
}
