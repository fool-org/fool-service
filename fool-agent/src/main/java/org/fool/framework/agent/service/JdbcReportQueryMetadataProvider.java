package org.fool.framework.agent.service;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcReportQueryMetadataProvider implements ReportQueryMetadataProvider {
    private final JdbcTemplate jdbcTemplate;

    public JdbcReportQueryMetadataProvider(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReportQueryMetadataSnapshot load(Long viewId) {
        if (viewId == null) {
            return ReportQueryMetadataSnapshot.unavailable(null, "view-id-required", "ViewId is required.");
        }
        try {
            List<ViewRow> views = jdbcTemplate.query("""
                            SELECT v.id AS view_id,
                                   v.view_name,
                                   v.view_title,
                                   v.view_text,
                                   v.view_model
                            FROM fool_sys_view v
                            WHERE v.id = ?
                            """,
                    this::viewRow,
                    viewId);
            if (views.isEmpty()) {
                return ReportQueryMetadataSnapshot.unavailable(viewId, "view-not-found", "View metadata was not found.");
            }
            ViewRow view = views.get(0);
            ModelRow model = modelFor(view.viewModel());
            LegacyViewRow legacyView = legacyViewFor(view.viewId());
            return ReportQueryMetadataSnapshot.hydrated(
                    view.viewId(),
                    view.viewName(),
                    view.viewTitle(),
                    view.viewText(),
                    view.viewModel(),
                    model.modelId(),
                    model.modelName(),
                    model.modelTable(),
                    legacyView.legacyViewType(),
                    legacyView.defaultDetailViewId(),
                    legacyView.viewCanEdit(),
                    legacyView.autoFreshInterval(),
                    columns(view.viewId(), model.modelId()),
                    operations(view.viewId()));
        } catch (DataAccessException ex) {
            return ReportQueryMetadataSnapshot.unavailable(
                    viewId,
                    "metadata-read-failed",
                    "Failed to read View/model metadata: " + failureMessage(ex));
        }
    }

    private String failureMessage(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return ex.getClass().getSimpleName();
    }

    private LegacyViewRow legacyViewFor(Long viewId) {
        List<LegacyViewRow> rows = jdbcTemplate.query("""
                        SELECT v.VIEW_TYPE AS legacy_view_type,
                               v.VIEW_DEFAULT AS default_detail_view_id,
                               v.VIEW_CANEDIT AS view_can_edit,
                               v.VIEW_AUTOFRESHINTERVAL AS auto_fresh_interval
                        FROM SW_SYS_VIEW v
                        WHERE v.VIEW_ID = ?
                        """,
                this::legacyViewRow,
                viewId);
        return rows.isEmpty() ? LegacyViewRow.empty() : rows.get(0);
    }

    private ModelRow modelFor(String viewModel) {
        if (viewModel == null || viewModel.isBlank()) {
            return ModelRow.empty();
        }
        Long modelId = numericId(viewModel);
        List<ModelRow> rows = modelId == null
                ? jdbcTemplate.query("""
                                SELECT id AS model_id,
                                       name AS model_name,
                                       table_name AS model_table
                                FROM fool_sys_model
                                WHERE name = ?
                                """,
                        this::modelRow,
                        viewModel.trim())
                : jdbcTemplate.query("""
                                SELECT id AS model_id,
                                       name AS model_name,
                                       table_name AS model_table
                                FROM fool_sys_model
                                WHERE id = ?
                                """,
                        this::modelRow,
                        modelId);
        return rows.isEmpty() ? ModelRow.empty() : rows.get(0);
    }

    private List<ReportQueryMetadataColumn> columns(Long viewId, Long modelId) {
        return jdbcTemplate.query("""
                        SELECT vi.id AS view_item_id,
                               vi.item_name,
                               vi.item_label,
                               vi.model_property,
                               vi.show_index,
                               vi.can_edit,
                               vi.input_type,
                               vi.edit_type,
                               vi.width,
                               vi.source_expression,
                               vi.list_view_id,
                               vi.edit_view_id,
                               vi.selected_view_id,
                               p.id AS property_id,
                               p.name AS property_name,
                               p.remark AS property_remark,
                               p.`column` AS db_column,
                               p.property_type,
                               p.is_collection
                        FROM fool_sys_view_item vi
                        LEFT JOIN fool_sys_model_property p
                          ON p.owner = ? AND p.name = vi.model_property
                        WHERE vi.view_id = ?
                        ORDER BY vi.show_index ASC, vi.id ASC
                        """,
                this::column,
                modelId,
                viewId);
    }

    private List<ReportQueryMetadataOperation> operations(Long viewId) {
        return jdbcTemplate.query("""
                        SELECT vo.SysId AS view_operation_id,
                               vo.SW_VIEW_OPERATION_NAME AS operation_name,
                               opv.SW_SYS_OPVIEW_OPREATION AS model_operation_id,
                               vo.SW_VIEW_OPERATION_RESULTVIEW AS result_view_id,
                               vo.SW_VIEW_OPERATION_INDEX AS location,
                               vo.SW_VIEW_OPERATION_REQUIRESELECTB AS require_select,
                               opv.SysId AS operation_view_id,
                               opv.SW_SYS_OPVIEW_SUCCESMSG AS success_msg,
                               opv.SW_SYS_OPVIEW_ERRORMSG AS error_msg,
                               opv.SW_SYS_OPVIEW_ConfirmMSG AS confirm_msg
                        FROM SW_SYS_VIEW_OPERATION vo
                        LEFT JOIN SW_SYS_OPERATIONVIEW opv
                          ON opv.SysId = vo.SW_VIEW_OPERATION_MODELOPERATION
                        WHERE vo.SW_SYS_VIEW_OperationsVIEW_ID = ?
                        ORDER BY vo.SW_VIEW_OPERATION_INDEX ASC, vo.SysId ASC
                        """,
                this::operation,
                viewId);
    }

    private ViewRow viewRow(ResultSet rs, int rowNum) throws SQLException {
        return new ViewRow(
                rs.getLong("view_id"),
                rs.getString("view_name"),
                rs.getString("view_title"),
                rs.getString("view_text"),
                rs.getString("view_model"));
    }

    private LegacyViewRow legacyViewRow(ResultSet rs, int rowNum) throws SQLException {
        return new LegacyViewRow(
                nullableInt(rs, "legacy_view_type"),
                nullableLong(rs, "default_detail_view_id"),
                nullableBoolean(rs, "view_can_edit"),
                nullableInt(rs, "auto_fresh_interval"));
    }

    private ModelRow modelRow(ResultSet rs, int rowNum) throws SQLException {
        return new ModelRow(
                nullableLong(rs, "model_id"),
                rs.getString("model_name"),
                rs.getString("model_table"));
    }

    private ReportQueryMetadataColumn column(ResultSet rs, int rowNum) throws SQLException {
        return new ReportQueryMetadataColumn(
                nullableLong(rs, "view_item_id"),
                rs.getString("item_name"),
                rs.getString("item_label"),
                rs.getString("model_property"),
                nullableInt(rs, "show_index"),
                nullableBoolean(rs, "can_edit"),
                nullableInt(rs, "input_type"),
                nullableInt(rs, "edit_type"),
                nullableInt(rs, "width"),
                rs.getString("source_expression"),
                nullableLong(rs, "list_view_id"),
                nullableLong(rs, "edit_view_id"),
                nullableLong(rs, "selected_view_id"),
                nullableLong(rs, "property_id"),
                rs.getString("property_name"),
                rs.getString("property_remark"),
                rs.getString("db_column"),
                nullableInt(rs, "property_type"),
                nullableBoolean(rs, "is_collection"));
    }

    private ReportQueryMetadataOperation operation(ResultSet rs, int rowNum) throws SQLException {
        return new ReportQueryMetadataOperation(
                nullableLong(rs, "view_operation_id"),
                rs.getString("operation_name"),
                nullableLong(rs, "model_operation_id"),
                nullableLong(rs, "result_view_id"),
                nullableInt(rs, "location"),
                nullableBoolean(rs, "require_select"),
                nullableLong(rs, "operation_view_id"),
                rs.getString("success_msg"),
                rs.getString("error_msg"),
                rs.getString("confirm_msg"));
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

    private Long numericId(String value) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private record ViewRow(Long viewId,
                           String viewName,
                           String viewTitle,
                           String viewText,
                           String viewModel) {
    }

    private record LegacyViewRow(Integer legacyViewType,
                                 Long defaultDetailViewId,
                                 Boolean viewCanEdit,
                                 Integer autoFreshInterval) {
        private static LegacyViewRow empty() {
            return new LegacyViewRow(null, null, null, null);
        }
    }

    private record ModelRow(Long modelId, String modelName, String modelTable) {
        private static ModelRow empty() {
            return new ModelRow(null, null, null);
        }
    }
}
