package org.fool.framework.agent.service;

public class AgentModelRelationMetadata {
    private final Integer relationType;
    private final Long sourcePropertyId;
    private final String sourcePropertyName;
    private final Long sourceModelId;
    private final Long targetPropertyId;
    private final String targetPropertyName;
    private final Long targetModelId;
    private final String tableName;
    private final String sourceColumn;
    private final String targetColumn;
    private final Boolean nullable;

    public AgentModelRelationMetadata(Integer relationType,
                                      Long sourcePropertyId,
                                      String sourcePropertyName,
                                      Long sourceModelId,
                                      Long targetPropertyId,
                                      String targetPropertyName,
                                      Long targetModelId,
                                      String tableName,
                                      String sourceColumn,
                                      String targetColumn,
                                      Boolean nullable) {
        this.relationType = relationType;
        this.sourcePropertyId = sourcePropertyId;
        this.sourcePropertyName = sourcePropertyName;
        this.sourceModelId = sourceModelId;
        this.targetPropertyId = targetPropertyId;
        this.targetPropertyName = targetPropertyName;
        this.targetModelId = targetModelId;
        this.tableName = tableName;
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
        this.nullable = nullable;
    }

    public Integer getRelationType() {
        return relationType;
    }

    public Long getSourcePropertyId() {
        return sourcePropertyId;
    }

    public String getSourcePropertyName() {
        return sourcePropertyName;
    }

    public Long getSourceModelId() {
        return sourceModelId;
    }

    public Long getTargetPropertyId() {
        return targetPropertyId;
    }

    public String getTargetPropertyName() {
        return targetPropertyName;
    }

    public Long getTargetModelId() {
        return targetModelId;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public Boolean getNullable() {
        return nullable;
    }
}
