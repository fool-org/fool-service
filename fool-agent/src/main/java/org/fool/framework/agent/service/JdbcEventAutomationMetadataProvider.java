package org.fool.framework.agent.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcEventAutomationMetadataProvider implements EventAutomationMetadataProvider {
    private final JdbcTemplate jdbcTemplate;

    public JdbcEventAutomationMetadataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AgentEventAutomationMetadataSnapshot load() {
        try {
            return AgentEventAutomationMetadataSnapshot.hydrated(definitions());
        } catch (DataAccessException ex) {
            return AgentEventAutomationMetadataSnapshot.unavailable(
                    "metadata-read-failed",
                    "Failed to read event/automation metadata: " + failureMessage(ex));
        }
    }

    private List<AgentEventDefinitionMetadata> definitions() {
        return jdbcTemplate.query("""
                        SELECT def.`EVTDEF_ID`,
                               def.`EVTDEF_FILTER`,
                               def.`EVTDEF_VIEW`,
                               view_meta.`view_name`,
                               def.`EVTDEF_OPERATION`,
                               def.`EVTDEF_MSGFMT`,
                               def.`EVTDEF_TIMEOUTSECS`,
                               def.`EVTDEF_MODEL`,
                               def.`EVTDEF_MODELREF`,
                               def.`EVTDEF_STATE`,
                               model.`name` AS model_name,
                               model.`table_name`,
                               CASE
                                 WHEN property.`column` IS NOT NULL AND property.`column` <> '' THEN property.`column`
                                 WHEN model.`auto_sys_id` = 1 THEN 'SYSID'
                                 ELSE 'ID'
                               END AS object_id_column,
                               (
                                 SELECT COUNT(*)
                                 FROM `SW_APP_AUTH_USER_SW_EVT_DEF` rel
                                 WHERE rel.`SW_EVT_DEF_ID` COLLATE utf8mb4_unicode_ci =
                                       def.`EVTDEF_ID` COLLATE utf8mb4_unicode_ci
                               ) AS notify_user_count,
                               (
                                 SELECT COUNT(*)
                                 FROM `SW_APP_AUTH_ROLE_SW_EVT_DEF` rel
                                 WHERE rel.`SW_EVT_DEF_ID` COLLATE utf8mb4_unicode_ci =
                                       def.`EVTDEF_ID` COLLATE utf8mb4_unicode_ci
                               ) AS notify_role_count,
                               (
                                 SELECT COUNT(*)
                                 FROM `SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF` rel
                                 WHERE rel.`SW_EVT_DEF_ID` COLLATE utf8mb4_unicode_ci =
                                       def.`EVTDEF_ID` COLLATE utf8mb4_unicode_ci
                               ) AS notify_department_count,
                               (
                                 SELECT COUNT(*)
                                 FROM `SW_APP_AUTH_COMPANY_SW_EVT_DEF` rel
                                 WHERE rel.`SW_EVT_DEF_ID` COLLATE utf8mb4_unicode_ci =
                                       def.`EVTDEF_ID` COLLATE utf8mb4_unicode_ci
                               ) AS notify_company_count,
                               (
                                 SELECT COUNT(*)
                                 FROM `SW_EVT_EVENT` event_record
                                 WHERE event_record.`EVT_Defination` COLLATE utf8mb4_unicode_ci =
                                       def.`EVTDEF_ID` COLLATE utf8mb4_unicode_ci
                               ) AS existing_event_count
                          FROM `SW_EVT_DEF` def
                          LEFT JOIN `fool_sys_model` model
                            ON CAST(model.`id` AS CHAR) COLLATE utf8mb4_unicode_ci =
                               def.`EVTDEF_MODEL` COLLATE utf8mb4_unicode_ci
                            OR model.`name` COLLATE utf8mb4_unicode_ci = def.`EVTDEF_MODEL` COLLATE utf8mb4_unicode_ci
                          LEFT JOIN `fool_sys_model_property` property
                            ON model.`id_property` = property.`id`
                          LEFT JOIN `fool_sys_view` view_meta
                            ON CAST(view_meta.`id` AS CHAR) COLLATE utf8mb4_unicode_ci =
                               def.`EVTDEF_VIEW` COLLATE utf8mb4_unicode_ci
                            OR view_meta.`view_name` COLLATE utf8mb4_unicode_ci = def.`EVTDEF_VIEW` COLLATE utf8mb4_unicode_ci
                         ORDER BY def.`EVTDEF_STATE` ASC, def.`EVTDEF_ID` ASC
                        """,
                this::definition);
    }

    private AgentEventDefinitionMetadata definition(ResultSet rs, int rowNum) throws SQLException {
        Integer modelRefType = nullableInt(rs, "EVTDEF_MODELREF");
        Integer state = nullableInt(rs, "EVTDEF_STATE");
        return new AgentEventDefinitionMetadata(
                rs.getString("EVTDEF_ID"),
                rs.getString("EVTDEF_FILTER"),
                rs.getString("EVTDEF_VIEW"),
                rs.getString("view_name"),
                rs.getString("EVTDEF_OPERATION"),
                rs.getString("EVTDEF_MSGFMT"),
                nullableInt(rs, "EVTDEF_TIMEOUTSECS"),
                rs.getString("EVTDEF_MODEL"),
                rs.getString("model_name"),
                rs.getString("table_name"),
                rs.getString("object_id_column"),
                modelRefType,
                modelRefTypeName(modelRefType),
                state,
                eventStateName(state),
                nullableInt(rs, "notify_user_count"),
                nullableInt(rs, "notify_role_count"),
                nullableInt(rs, "notify_department_count"),
                nullableInt(rs, "notify_company_count"),
                nullableInt(rs, "existing_event_count"));
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private String modelRefTypeName(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> "SysModel";
            case 1 -> "AppModel";
            case 2 -> "DbModel";
            default -> "Unknown";
        };
    }

    private String eventStateName(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> "IsRunning";
            case 1 -> "Stopped";
            default -> "Unknown";
        };
    }

    private String failureMessage(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return ex.getClass().getSimpleName();
    }
}
