package org.fool.framework.agent.service;

import java.util.List;

public class ReportQueryMetadataSnapshot {
    private final Long viewId;
    private final String viewName;
    private final String viewTitle;
    private final String viewText;
    private final String viewModel;
    private final Long modelId;
    private final String modelName;
    private final String modelTable;
    private final Integer legacyViewType;
    private final Long defaultDetailViewId;
    private final Boolean viewCanEdit;
    private final Integer autoFreshInterval;
    private final String status;
    private final String reason;
    private final List<ReportQueryMetadataColumn> columns;
    private final List<ReportQueryMetadataOperation> operations;

    public ReportQueryMetadataSnapshot(Long viewId,
                                       String viewName,
                                       String viewTitle,
                                       String viewText,
                                       String viewModel,
                                       Long modelId,
                                       String modelName,
                                       String modelTable,
                                       Integer legacyViewType,
                                       Long defaultDetailViewId,
                                       Boolean viewCanEdit,
                                       Integer autoFreshInterval,
                                       String status,
                                       String reason,
                                       List<ReportQueryMetadataColumn> columns,
                                       List<ReportQueryMetadataOperation> operations) {
        this.viewId = viewId;
        this.viewName = viewName;
        this.viewTitle = viewTitle;
        this.viewText = viewText;
        this.viewModel = viewModel;
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelTable = modelTable;
        this.legacyViewType = legacyViewType;
        this.defaultDetailViewId = defaultDetailViewId;
        this.viewCanEdit = viewCanEdit;
        this.autoFreshInterval = autoFreshInterval;
        this.status = status;
        this.reason = reason;
        this.columns = columns == null ? List.of() : List.copyOf(columns);
        this.operations = operations == null ? List.of() : List.copyOf(operations);
    }

    public static ReportQueryMetadataSnapshot hydrated(Long viewId,
                                                       String viewName,
                                                       String viewTitle,
                                                       String viewText,
                                                       String viewModel,
                                                       Long modelId,
                                                       String modelName,
                                                       String modelTable,
                                                       List<ReportQueryMetadataColumn> columns) {
        return hydrated(
                viewId,
                viewName,
                viewTitle,
                viewText,
                viewModel,
                modelId,
                modelName,
                modelTable,
                null,
                null,
                null,
                null,
                columns,
                List.of());
    }

    public static ReportQueryMetadataSnapshot hydrated(Long viewId,
                                                       String viewName,
                                                       String viewTitle,
                                                       String viewText,
                                                       String viewModel,
                                                       Long modelId,
                                                       String modelName,
                                                       String modelTable,
                                                       Integer legacyViewType,
                                                       Long defaultDetailViewId,
                                                       Boolean viewCanEdit,
                                                       Integer autoFreshInterval,
                                                       List<ReportQueryMetadataColumn> columns,
                                                       List<ReportQueryMetadataOperation> operations) {
        return new ReportQueryMetadataSnapshot(
                viewId,
                viewName,
                viewTitle,
                viewText,
                viewModel,
                modelId,
                modelName,
                modelTable,
                legacyViewType,
                defaultDetailViewId,
                viewCanEdit,
                autoFreshInterval,
                "hydrated",
                null,
                columns,
                operations);
    }

    public static ReportQueryMetadataSnapshot unavailable(Long viewId, String status, String reason) {
        return new ReportQueryMetadataSnapshot(
                viewId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                status,
                reason,
                List.of(),
                List.of());
    }

    public Long getViewId() {
        return viewId;
    }

    public String getViewName() {
        return viewName;
    }

    public String getViewTitle() {
        return viewTitle;
    }

    public String getViewText() {
        return viewText;
    }

    public String getViewModel() {
        return viewModel;
    }

    public Long getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelTable() {
        return modelTable;
    }

    public Integer getLegacyViewType() {
        return legacyViewType;
    }

    public Long getDefaultDetailViewId() {
        return defaultDetailViewId;
    }

    public Boolean getViewCanEdit() {
        return viewCanEdit;
    }

    public Integer getAutoFreshInterval() {
        return autoFreshInterval;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<ReportQueryMetadataColumn> getColumns() {
        return columns;
    }

    public List<ReportQueryMetadataOperation> getOperations() {
        return operations;
    }

    public List<ReportQueryMetadataColumn> reportableColumns() {
        return columns.stream()
                .filter(ReportQueryMetadataColumn::isReportable)
                .toList();
    }

    public boolean isHydrated() {
        return "hydrated".equals(status);
    }
}
