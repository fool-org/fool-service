package org.fool.framework.agent.service;

import java.util.List;

public class AgentModelMetadataSnapshot {
    private final Long modelId;
    private final String modelName;
    private final String modelText;
    private final String remark;
    private final Integer modelType;
    private final String className;
    private final String tableName;
    private final Boolean autoSysId;
    private final Long idPropertyId;
    private final Long defaultOwnerId;
    private final String status;
    private final String reason;
    private final List<AgentModelPropertyMetadata> properties;
    private final List<AgentModelRelationMetadata> relations;
    private final List<AgentModelOperationMetadata> operations;

    public AgentModelMetadataSnapshot(Long modelId,
                                      String modelName,
                                      String modelText,
                                      String remark,
                                      Integer modelType,
                                      String className,
                                      String tableName,
                                      Boolean autoSysId,
                                      Long idPropertyId,
                                      Long defaultOwnerId,
                                      String status,
                                      String reason,
                                      List<AgentModelPropertyMetadata> properties,
                                      List<AgentModelRelationMetadata> relations,
                                      List<AgentModelOperationMetadata> operations) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelText = modelText;
        this.remark = remark;
        this.modelType = modelType;
        this.className = className;
        this.tableName = tableName;
        this.autoSysId = autoSysId;
        this.idPropertyId = idPropertyId;
        this.defaultOwnerId = defaultOwnerId;
        this.status = status;
        this.reason = reason;
        this.properties = properties == null ? List.of() : List.copyOf(properties);
        this.relations = relations == null ? List.of() : List.copyOf(relations);
        this.operations = operations == null ? List.of() : List.copyOf(operations);
    }

    public static AgentModelMetadataSnapshot hydrated(Long modelId,
                                                      String modelName,
                                                      String modelText,
                                                      String remark,
                                                      Integer modelType,
                                                      String className,
                                                      String tableName,
                                                      Boolean autoSysId,
                                                      Long idPropertyId,
                                                      Long defaultOwnerId,
                                                      List<AgentModelPropertyMetadata> properties,
                                                      List<AgentModelRelationMetadata> relations,
                                                      List<AgentModelOperationMetadata> operations) {
        return new AgentModelMetadataSnapshot(
                modelId,
                modelName,
                modelText,
                remark,
                modelType,
                className,
                tableName,
                autoSysId,
                idPropertyId,
                defaultOwnerId,
                "hydrated",
                null,
                properties,
                relations,
                operations);
    }

    public static AgentModelMetadataSnapshot unavailable(Long modelId, String modelName, String status, String reason) {
        return new AgentModelMetadataSnapshot(
                modelId,
                modelName,
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
                List.of(),
                List.of());
    }

    public Long getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelText() {
        return modelText;
    }

    public String getRemark() {
        return remark;
    }

    public Integer getModelType() {
        return modelType;
    }

    public String getClassName() {
        return className;
    }

    public String getTableName() {
        return tableName;
    }

    public Boolean getAutoSysId() {
        return autoSysId;
    }

    public Long getIdPropertyId() {
        return idPropertyId;
    }

    public Long getDefaultOwnerId() {
        return defaultOwnerId;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<AgentModelPropertyMetadata> getProperties() {
        return properties;
    }

    public List<AgentModelRelationMetadata> getRelations() {
        return relations;
    }

    public List<AgentModelOperationMetadata> getOperations() {
        return operations;
    }

    public boolean isHydrated() {
        return "hydrated".equals(status);
    }
}
