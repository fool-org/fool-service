package org.fool.framework.query;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JdbcQueryExecutor {
    private final JdbcTemplate jdbcTemplate;

    public JdbcQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcQueryExecutor(String connectionString) {
        this(createJdbcTemplate(connectionString));
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
        QueryResult result = new QueryResult(
                pageSize,
                (size, page) -> loadPage(instance, size, page, rowIndex, includeRowIndex));
        result.setCurrentPage(startPage);
        QueryResult.Page page = loadPage(instance, pageSize, startPage, rowIndex, includeRowIndex);
        result.updatePage(page.totalRecords(), page.rows());
        return result;
    }

    private QueryResult.Page loadPage(
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
        return new QueryResult.Page(totalRecords == null ? 0L : totalRecords, rows);
    }

    private static JdbcTemplate createJdbcTemplate(String connectionString) {
        ConnectionSettings settings = parse(connectionString);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(settings.url());
        if (settings.username() != null) {
            dataSource.setUsername(settings.username());
        }
        if (settings.password() != null) {
            dataSource.setPassword(settings.password());
        }
        if (settings.driverClassName() != null) {
            dataSource.setDriverClassName(settings.driverClassName());
        }
        return new JdbcTemplate(dataSource);
    }

    static ConnectionSettings parse(String connectionString) {
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalArgumentException("Query database connection string is required.");
        }
        String trimmed = connectionString.trim();
        if (trimmed.startsWith("jdbc:")) {
            return new ConnectionSettings(trimmed, null, null, null);
        }

        Map<String, String> values = new LinkedHashMap<>();
        for (String part : trimmed.split(";")) {
            if (part.isBlank() || !part.contains("=")) {
                continue;
            }
            String[] pair = part.split("=", 2);
            values.put(pair[0].trim().toLowerCase(Locale.ROOT), pair[1].trim());
        }
        String url = first(values, "url", "jdbcurl", "jdbc-url", "jdbc_url");
        if (url == null || url.isBlank()) {
            url = legacySqlServerUrl(values);
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Query database JDBC url is required.");
        }
        return new ConnectionSettings(
                url,
                first(values, "username", "user", "user id", "userid", "uid"),
                first(values, "password", "pwd"),
                first(values, "driver", "driverclassname", "driver-class-name"));
    }

    private static String legacySqlServerUrl(Map<String, String> values) {
        String dataSource = first(values, "data source", "server", "address", "addr", "network address");
        if (dataSource == null || dataSource.isBlank()) {
            return null;
        }

        StringBuilder url = new StringBuilder("jdbc:sqlserver://").append(dataSource);
        String database = first(values, "initial catalog", "database", "database name");
        if (database != null && !database.isBlank()) {
            url.append(";databaseName=").append(database);
        }
        String integratedSecurity = first(values, "integrated security", "integratedsecurity", "trusted_connection");
        if (isTruthy(integratedSecurity)) {
            url.append(";integratedSecurity=true");
        }
        return url.toString();
    }

    private static boolean isTruthy(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(normalized) || "sspi".equals(normalized) || "yes".equals(normalized);
    }

    private static String first(Map<String, String> values, String... keys) {
        for (String key : keys) {
            String value = values.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    record ConnectionSettings(String url, String username, String password, String driverClassName) {
    }
}
