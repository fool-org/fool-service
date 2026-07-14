package org.fool.framework.agent.service;

public class AgentEventDefinitionMetadata {
    private final String definitionId;
    private final String filter;
    private final String viewId;
    private final String viewName;
    private final String operationId;
    private final String messageFormat;
    private final Integer timeoutSeconds;
    private final String modelId;
    private final String modelName;
    private final String tableName;
    private final String objectIdColumn;
    private final Integer modelRefType;
    private final String modelRefTypeName;
    private final Integer state;
    private final String stateName;
    private final Integer notifyUserCount;
    private final Integer notifyRoleCount;
    private final Integer notifyDepartmentCount;
    private final Integer notifyCompanyCount;
    private final Integer existingEventCount;

    public AgentEventDefinitionMetadata(String definitionId,
                                        String filter,
                                        String viewId,
                                        String viewName,
                                        String operationId,
                                        String messageFormat,
                                        Integer timeoutSeconds,
                                        String modelId,
                                        String modelName,
                                        String tableName,
                                        String objectIdColumn,
                                        Integer modelRefType,
                                        String modelRefTypeName,
                                        Integer state,
                                        String stateName,
                                        Integer notifyUserCount,
                                        Integer notifyRoleCount,
                                        Integer notifyDepartmentCount,
                                        Integer notifyCompanyCount,
                                        Integer existingEventCount) {
        this.definitionId = definitionId;
        this.filter = filter;
        this.viewId = viewId;
        this.viewName = viewName;
        this.operationId = operationId;
        this.messageFormat = messageFormat;
        this.timeoutSeconds = timeoutSeconds;
        this.modelId = modelId;
        this.modelName = modelName;
        this.tableName = tableName;
        this.objectIdColumn = objectIdColumn;
        this.modelRefType = modelRefType;
        this.modelRefTypeName = modelRefTypeName;
        this.state = state;
        this.stateName = stateName;
        this.notifyUserCount = notifyUserCount;
        this.notifyRoleCount = notifyRoleCount;
        this.notifyDepartmentCount = notifyDepartmentCount;
        this.notifyCompanyCount = notifyCompanyCount;
        this.existingEventCount = existingEventCount;
    }

    public String getDefinitionId() {
        return definitionId;
    }

    public String getFilter() {
        return filter;
    }

    public String getViewId() {
        return viewId;
    }

    public String getViewName() {
        return viewName;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getObjectIdColumn() {
        return objectIdColumn;
    }

    public Integer getModelRefType() {
        return modelRefType;
    }

    public String getModelRefTypeName() {
        return modelRefTypeName;
    }

    public Integer getState() {
        return state;
    }

    public String getStateName() {
        return stateName;
    }

    public Integer getNotifyUserCount() {
        return notifyUserCount;
    }

    public Integer getNotifyRoleCount() {
        return notifyRoleCount;
    }

    public Integer getNotifyDepartmentCount() {
        return notifyDepartmentCount;
    }

    public Integer getNotifyCompanyCount() {
        return notifyCompanyCount;
    }

    public Integer getExistingEventCount() {
        return existingEventCount;
    }
}
