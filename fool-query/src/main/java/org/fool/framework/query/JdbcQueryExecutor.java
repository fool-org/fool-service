package org.fool.framework.query;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class JdbcQueryExecutor {
    private final JdbcTemplate jdbcTemplate;

    public JdbcQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public QueryResult execute(QueryInstance instance, int pageSize, int startPage) {
        return execute(instance, pageSize, startPage, "RowIndex", false);
    }

    public QueryResult execute(
            QueryInstance instance,
            int pageSize,
            int startPage,
            String rowIndex,
            boolean includeRowIndex) {
        PagedQuerySql querySql = QuerySqlBuilder.pagedQuerySql(
                instance,
                pageSize,
                startPage,
                rowIndex,
                includeRowIndex);

        Long totalRecords = jdbcTemplate.queryForObject(
                querySql.getCountSql(),
                Long.class,
                querySql.getCountArgs());
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                querySql.getPageSql(),
                querySql.getPageArgs());

        QueryResult result = new QueryResult(pageSize);
        result.setCurrentPage(startPage);
        result.updatePage(totalRecords == null ? 0L : totalRecords, rows);
        return result;
    }
}
