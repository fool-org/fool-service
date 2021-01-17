package org.fool.framework.dao;


import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface GetFieldFunction<T> {
    T get(ResultSet resultSet, String columnName) throws SQLException;
}
