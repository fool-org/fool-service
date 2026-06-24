package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class JdbcEventApplicationCatalog implements EventApplicationCatalog {
    static final String APPLICATION_ID_COLUMN = "SW_APP_APPLICATIONID";
    static final String APPLICATION_RELATION_ID_COLUMN = "SW_APPLICATION_ID";
    static final String STORE_CONNECTION_COLUMN = "SW_STORE_CON";
    static final String SYSTEM_CONNECTION_COLUMN = "SW_APP_CON";
    static final String SELECT_APPLICATIONS_SQL = """
            SELECT `SW_APP_APPLICATIONID`, `SW_APP_CON`
            FROM `SW_APPLICATION`
            ORDER BY `SW_APP_APPLICATIONID`
            """;
    static final String SELECT_DATABASE_CONNECTIONS_SQL_TEMPLATE = """
            SELECT rel.`SW_APPLICATION_ID`, store.`SW_STORE_CON`
            FROM `SW_APPLICATION_SW_STOREDB` rel
            JOIN `SW_STOREDB` store
              ON rel.`SW_STOREDB_ID` = store.`SW_STORE_STOREID`
            WHERE rel.`SW_APPLICATION_ID` IN (%s)
            ORDER BY rel.`SW_APPLICATION_ID`, rel.`SW_STOREDB_ID`
            """;

    private final Supplier<List<Map<String, Object>>> applicationRows;
    private final Function<List<String>, List<Map<String, Object>>> databaseRows;

    @Autowired
    public JdbcEventApplicationCatalog(JdbcTemplate jdbcTemplate) {
        this(
                () -> jdbcTemplate.queryForList(SELECT_APPLICATIONS_SQL),
                applicationIds -> queryForDatabaseRows(jdbcTemplate, applicationIds));
    }

    JdbcEventApplicationCatalog(
            Supplier<List<Map<String, Object>>> applicationRows,
            Function<List<String>, List<Map<String, Object>>> databaseRows) {
        this.applicationRows = applicationRows;
        this.databaseRows = databaseRows;
    }

    @Override
    public List<EventApplicationScope> findApplications() {
        Map<String, ApplicationBuilder> applicationsById = new LinkedHashMap<>();
        for (Map<String, Object> row : applicationRows.get()) {
            String applicationId = valueOf(row.get(APPLICATION_ID_COLUMN));
            if (applicationId != null && !applicationId.isBlank()) {
                applicationsById.put(
                        applicationId,
                        new ApplicationBuilder(applicationId, valueOf(row.get(SYSTEM_CONNECTION_COLUMN))));
            }
        }
        if (applicationsById.isEmpty()) {
            return List.of();
        }
        for (Map<String, Object> row : databaseRows.apply(new ArrayList<>(applicationsById.keySet()))) {
            ApplicationBuilder application = applicationsById.get(valueOf(row.get(APPLICATION_RELATION_ID_COLUMN)));
            String databaseConnection = valueOf(row.get(STORE_CONNECTION_COLUMN));
            if (application != null && databaseConnection != null && !databaseConnection.isBlank()) {
                application.databaseConnections.add(databaseConnection);
            }
        }
        return applicationsById.values().stream()
                .map(ApplicationBuilder::build)
                .toList();
    }

    private static List<Map<String, Object>> queryForDatabaseRows(
            JdbcTemplate jdbcTemplate,
            List<String> applicationIds) {
        if (applicationIds.isEmpty()) {
            return List.of();
        }
        String sql = String.format(SELECT_DATABASE_CONNECTIONS_SQL_TEMPLATE, placeholders(applicationIds.size()));
        return jdbcTemplate.queryForList(sql, applicationIds.toArray());
    }

    private static String placeholders(int size) {
        return IntStream.range(0, size)
                .mapToObj(index -> "?")
                .collect(Collectors.joining(", "));
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }

    private static final class ApplicationBuilder {
        private final String applicationId;
        private final String systemConnection;
        private final List<String> databaseConnections = new ArrayList<>();

        private ApplicationBuilder(String applicationId, String systemConnection) {
            this.applicationId = applicationId;
            this.systemConnection = systemConnection;
        }

        private EventApplicationScope build() {
            return new EventApplicationScope(applicationId, systemConnection, databaseConnections);
        }
    }
}
