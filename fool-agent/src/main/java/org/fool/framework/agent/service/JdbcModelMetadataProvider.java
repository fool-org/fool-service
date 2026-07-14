package org.fool.framework.agent.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcModelMetadataProvider implements ModelMetadataProvider {
    private final JdbcTemplate jdbcTemplate;

    public JdbcModelMetadataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AgentModelMetadataSnapshot load(Long modelId, String modelName) {
        if (modelId == null && !StringUtils.hasText(modelName)) {
            return AgentModelMetadataSnapshot.unavailable(
                    null,
                    null,
                    "model-id-or-name-required",
                    "ModelId or ModelName is required.");
        }
        try {
            List<ModelRow> models = modelId == null ? modelByName(modelName) : modelById(modelId);
            if (models.isEmpty()) {
                return AgentModelMetadataSnapshot.unavailable(
                        modelId,
                        modelName,
                        "model-not-found",
                        "Model metadata was not found.");
            }
            ModelRow model = models.get(0);
            return AgentModelMetadataSnapshot.hydrated(
                    model.modelId(),
                    model.modelName(),
                    model.modelText(),
                    model.remark(),
                    model.modelType(),
                    model.className(),
                    model.tableName(),
                    model.autoSysId(),
                    model.idPropertyId(),
                    model.defaultOwnerId(),
                    properties(model.modelId()),
                    relations(model.modelId()),
                    operations(model.modelId()));
        } catch (DataAccessException ex) {
            return AgentModelMetadataSnapshot.unavailable(
                    modelId,
                    modelName,
                    "metadata-read-failed",
                    "Failed to read model metadata: " + failureMessage(ex));
        }
    }

    private List<ModelRow> modelById(Long modelId) {
        return jdbcTemplate.query("""
                        SELECT id AS model_id,
                               name AS model_name,
                               text AS model_text,
                               remark,
                               model_type,
                               class_name,
                               table_name,
                               auto_sys_id,
                               id_property,
                               default_owner
                        FROM fool_sys_model
                        WHERE id = ?
                        """,
                this::model,
                modelId);
    }

    private List<ModelRow> modelByName(String modelName) {
        return jdbcTemplate.query("""
                        SELECT id AS model_id,
                               name AS model_name,
                               text AS model_text,
                               remark,
                               model_type,
                               class_name,
                               table_name,
                               auto_sys_id,
                               id_property,
                               default_owner
                        FROM fool_sys_model
                        WHERE name = ?
                        """,
                this::model,
                modelName.trim());
    }

    private List<AgentModelPropertyMetadata> properties(Long modelId) {
        return jdbcTemplate.query("""
                        SELECT id AS property_id,
                               name,
                               remark,
                               property_model,
                               is_collection,
                               owner,
                               filter,
                               source,
                               format,
                               `column` AS db_column,
                               property_type,
                               allow_db_null,
                               is_check,
                               ix_group,
                               generation_type,
                               generation_expression,
                               default_value,
                               multi_map
                        FROM fool_sys_model_property
                        WHERE owner = ?
                        ORDER BY id ASC
                        """,
                this::property,
                modelId);
    }

    private List<AgentModelRelationMetadata> relations(Long modelId) {
        return jdbcTemplate.query("""
                        SELECT r.SW_SYS_RELATION_TYPE AS relation_type,
                               r.SW_SYS_RELATION_SOURCEPROPERTY AS source_property_id,
                               sp.name AS source_property_name,
                               sp.owner AS source_model_id,
                               r.SW_SYS_RELATION_TARGETPROPERTY AS target_property_id,
                               tp.name AS target_property_name,
                               tp.owner AS target_model_id,
                               r.SW_SYS_RELATION_TABLE AS relation_table,
                               r.SW_SYS_RELATION_SOURCECOL AS source_column,
                               r.SW_SYS_RELATION_TARGETCOL AS target_column,
                               r.SW_SYS_RELATION_CANBENULL AS nullable
                        FROM SW_SYS_RELATION r
                        LEFT JOIN fool_sys_model_property sp
                          ON sp.id = r.SW_SYS_RELATION_SOURCEPROPERTY
                        LEFT JOIN fool_sys_model_property tp
                          ON tp.id = r.SW_SYS_RELATION_TARGETPROPERTY
                        WHERE sp.owner = ? OR tp.owner = ?
                        ORDER BY r.SW_SYS_RELATION_SOURCEPROPERTY ASC,
                                 r.SW_SYS_RELATION_TARGETPROPERTY ASC
                        """,
                this::relation,
                modelId,
                modelId);
    }

    private List<AgentModelOperationMetadata> operations(Long modelId) {
        return jdbcTemplate.query("""
                        SELECT op.SysId AS operation_id,
                               op.SW_MODEL_OPERATION_NAME AS operation_name,
                               op.SW_MODEL_OPERATION_FILTER AS operation_filter,
                               op.SW_MODEL_OPERATION_BASETYPE AS base_type,
                               op.SW_MODEL_OPERATION_ARGMODEL AS arg_model,
                               op.SW_MODEL_OPERATION_ARGFILTER AS arg_filter,
                               op.SW_MODEL_OPERATION_INVOKEDLL AS invoke_dll,
                               op.SW_MODEL_OPERATION_INVOKECLASS AS invoke_class,
                               op.SW_MODEL_OPERATION_INVOKEMETHOD AS invoke_method,
                               op.SW_MODEL_OPERATION_RETURNMODEL AS return_model,
                               (
                                 SELECT COUNT(*)
                                 FROM SW_SYS_COMMANDS c
                                 WHERE c.SW_SYS_OPERATION_CommandsSysId = op.SysId
                               ) AS command_count
                        FROM SW_SYS_OPERATION op
                        WHERE op.SW_SYS_MODEL_OperationsMODEL_ID = ?
                        ORDER BY op.SysId ASC
                        """,
                this::operation,
                modelId);
    }

    private ModelRow model(ResultSet rs, int rowNum) throws SQLException {
        return new ModelRow(
                nullableLong(rs, "model_id"),
                rs.getString("model_name"),
                rs.getString("model_text"),
                rs.getString("remark"),
                nullableInt(rs, "model_type"),
                rs.getString("class_name"),
                rs.getString("table_name"),
                nullableBoolean(rs, "auto_sys_id"),
                nullableLong(rs, "id_property"),
                nullableLong(rs, "default_owner"));
    }

    private AgentModelPropertyMetadata property(ResultSet rs, int rowNum) throws SQLException {
        return new AgentModelPropertyMetadata(
                nullableLong(rs, "property_id"),
                rs.getString("name"),
                rs.getString("remark"),
                nullableLong(rs, "property_model"),
                nullableBoolean(rs, "is_collection"),
                nullableLong(rs, "owner"),
                rs.getString("filter"),
                rs.getString("source"),
                rs.getString("format"),
                rs.getString("db_column"),
                nullableInt(rs, "property_type"),
                nullableBoolean(rs, "allow_db_null"),
                nullableBoolean(rs, "is_check"),
                rs.getString("ix_group"),
                nullableInt(rs, "generation_type"),
                rs.getString("generation_expression"),
                rs.getString("default_value"),
                nullableBoolean(rs, "multi_map"));
    }

    private AgentModelRelationMetadata relation(ResultSet rs, int rowNum) throws SQLException {
        return new AgentModelRelationMetadata(
                nullableInt(rs, "relation_type"),
                nullableLong(rs, "source_property_id"),
                rs.getString("source_property_name"),
                nullableLong(rs, "source_model_id"),
                nullableLong(rs, "target_property_id"),
                rs.getString("target_property_name"),
                nullableLong(rs, "target_model_id"),
                rs.getString("relation_table"),
                rs.getString("source_column"),
                rs.getString("target_column"),
                nullableBoolean(rs, "nullable"));
    }

    private AgentModelOperationMetadata operation(ResultSet rs, int rowNum) throws SQLException {
        return new AgentModelOperationMetadata(
                nullableLong(rs, "operation_id"),
                rs.getString("operation_name"),
                rs.getString("operation_filter"),
                nullableInt(rs, "base_type"),
                nullableLong(rs, "arg_model"),
                rs.getString("arg_filter"),
                rs.getString("invoke_dll"),
                rs.getString("invoke_class"),
                rs.getString("invoke_method"),
                nullableLong(rs, "return_model"),
                nullableInt(rs, "command_count"));
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
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

    private record ModelRow(Long modelId,
                            String modelName,
                            String modelText,
                            String remark,
                            Integer modelType,
                            String className,
                            String tableName,
                            Boolean autoSysId,
                            Long idPropertyId,
                            Long defaultOwnerId) {
    }
}
