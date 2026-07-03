package org.fool.framework.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class DriverManagerEventJdbcTemplateFactory implements EventJdbcTemplateFactory {
    @Override
    public JdbcTemplate create(String connectionString) {
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
            throw new IllegalArgumentException("Event database connection string is required.");
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
            throw new IllegalArgumentException("Event database JDBC url is required.");
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
