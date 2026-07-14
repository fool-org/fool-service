package org.fool.framework.agent.service;

public class ReportQueryMetadataOperation {
    private final Long viewOperationId;
    private final String name;
    private final Long operationId;
    private final Long resultViewId;
    private final Integer location;
    private final Boolean requireSelect;
    private final Long operationViewId;
    private final String successMessage;
    private final String errorMessage;
    private final String confirmMessage;

    public ReportQueryMetadataOperation(Long viewOperationId,
                                        String name,
                                        Long operationId,
                                        Long resultViewId,
                                        Integer location,
                                        Boolean requireSelect,
                                        Long operationViewId,
                                        String successMessage,
                                        String errorMessage,
                                        String confirmMessage) {
        this.viewOperationId = viewOperationId;
        this.name = name;
        this.operationId = operationId;
        this.resultViewId = resultViewId;
        this.location = location;
        this.requireSelect = requireSelect;
        this.operationViewId = operationViewId;
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
        this.confirmMessage = confirmMessage;
    }

    public Long getViewOperationId() {
        return viewOperationId;
    }

    public String getName() {
        return name;
    }

    public Long getOperationId() {
        return operationId;
    }

    public Long getResultViewId() {
        return resultViewId;
    }

    public Integer getLocation() {
        return location;
    }

    public Boolean getRequireSelect() {
        return requireSelect;
    }

    public Long getOperationViewId() {
        return operationViewId;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getConfirmMessage() {
        return confirmMessage;
    }

    public String operationType() {
        return operationId == null ? "MODAL_DETAIL_VIEW" : "COMMAND";
    }
}
