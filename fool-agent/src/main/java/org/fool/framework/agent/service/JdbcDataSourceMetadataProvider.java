package org.fool.framework.agent.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcDataSourceMetadataProvider implements DataSourceMetadataProvider {
    private final JdbcTemplate jdbcTemplate;

    public JdbcDataSourceMetadataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AgentDataSourceMetadataSnapshot load() {
        try {
            return AgentDataSourceMetadataSnapshot.hydrated(
                    workingDatabases(),
                    applicationRoutes(),
                    dataSourceRoutes());
        } catch (DataAccessException ex) {
            return AgentDataSourceMetadataSnapshot.unavailable(
                    "metadata-read-failed",
                    "Failed to read data-source metadata: " + failureMessage(ex));
        }
    }

    private List<AgentWorkingDatabaseMetadata> workingDatabases() {
        return jdbcTemplate.query("""
                        SELECT `DBID`,
                               `DBName`,
                               `DBYear`,
                               `DBSysName`,
                               `IsActive`,
                               `DBNo`,
                               `UserName`,
                               `CompanyName`,
                               `ServerIp`,
                               `IsLocal`,
                               CASE
                                 WHEN `pwd5` IS NULL OR `pwd5` = '' THEN 0
                                 ELSE 1
                               END AS credential_configured
                          FROM `WorkDataBase`
                         ORDER BY `IsActive` DESC, `DBNo` ASC
                        """,
                this::workingDatabase);
    }

    private List<AgentApplicationRouteMetadata> applicationRoutes() {
        return jdbcTemplate.query("""
                        SELECT app.`BO_Id` AS app_id,
                               app.`BO_AppName` AS app_name,
                               appdb.`DBNo` AS db_no
                          FROM `DB_App` app
                          JOIN `DB_AppDB` appdb ON app.`BO_Id` = appdb.`App_Id`
                         ORDER BY app.`BO_AppName` ASC, appdb.`DBNo` ASC
                        """,
                this::applicationRoute);
    }

    private List<AgentDataSourceRouteMetadata> dataSourceRoutes() {
        return jdbcTemplate.query("""
                        SELECT `DS_Key` AS data_source_key,
                               `DS_DBNo` AS db_no
                          FROM `DS_DataSourceSet`
                         ORDER BY `DS_Key` ASC
                        """,
                this::dataSourceRoute);
    }

    private AgentWorkingDatabaseMetadata workingDatabase(ResultSet rs, int rowNum) throws SQLException {
        return new AgentWorkingDatabaseMetadata(
                nullableLong(rs, "DBID"),
                rs.getString("DBName"),
                rs.getString("DBYear"),
                rs.getString("DBSysName"),
                nullableBoolean(rs, "IsActive"),
                rs.getString("DBNo"),
                rs.getString("UserName"),
                rs.getString("CompanyName"),
                rs.getString("ServerIp"),
                nullableBoolean(rs, "IsLocal"),
                nullableBoolean(rs, "credential_configured"));
    }

    private AgentApplicationRouteMetadata applicationRoute(ResultSet rs, int rowNum) throws SQLException {
        return new AgentApplicationRouteMetadata(
                nullableLong(rs, "app_id"),
                rs.getString("app_name"),
                rs.getString("db_no"));
    }

    private AgentDataSourceRouteMetadata dataSourceRoute(ResultSet rs, int rowNum) throws SQLException {
        return new AgentDataSourceRouteMetadata(
                rs.getString("data_source_key"),
                rs.getString("db_no"));
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private Boolean nullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }

    private String failureMessage(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return ex.getClass().getSimpleName();
    }
}
