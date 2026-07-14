package org.fool.framework.agent.service;

public class AgentModelPropertyMetadata {
    private final Long propertyId;
    private final String name;
    private final String remark;
    private final Long propertyModelId;
    private final Boolean collection;
    private final Long ownerId;
    private final String filter;
    private final String source;
    private final String format;
    private final String dbColumn;
    private final Integer propertyType;
    private final Boolean allowDbNull;
    private final Boolean check;
    private final String ixGroup;
    private final Integer generationType;
    private final String generationExpression;
    private final String defaultValue;
    private final Boolean multiMap;

    public AgentModelPropertyMetadata(Long propertyId,
                                      String name,
                                      String remark,
                                      Long propertyModelId,
                                      Boolean collection,
                                      Long ownerId,
                                      String filter,
                                      String source,
                                      String format,
                                      String dbColumn,
                                      Integer propertyType,
                                      Boolean allowDbNull,
                                      Boolean check,
                                      String ixGroup,
                                      Integer generationType,
                                      String generationExpression,
                                      String defaultValue,
                                      Boolean multiMap) {
        this.propertyId = propertyId;
        this.name = name;
        this.remark = remark;
        this.propertyModelId = propertyModelId;
        this.collection = collection;
        this.ownerId = ownerId;
        this.filter = filter;
        this.source = source;
        this.format = format;
        this.dbColumn = dbColumn;
        this.propertyType = propertyType;
        this.allowDbNull = allowDbNull;
        this.check = check;
        this.ixGroup = ixGroup;
        this.generationType = generationType;
        this.generationExpression = generationExpression;
        this.defaultValue = defaultValue;
        this.multiMap = multiMap;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    public String getRemark() {
        return remark;
    }

    public Long getPropertyModelId() {
        return propertyModelId;
    }

    public Boolean getCollection() {
        return collection;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getFilter() {
        return filter;
    }

    public String getSource() {
        return source;
    }

    public String getFormat() {
        return format;
    }

    public String getDbColumn() {
        return dbColumn;
    }

    public Integer getPropertyType() {
        return propertyType;
    }

    public Boolean getAllowDbNull() {
        return allowDbNull;
    }

    public Boolean getCheck() {
        return check;
    }

    public String getIxGroup() {
        return ixGroup;
    }

    public Integer getGenerationType() {
        return generationType;
    }

    public String getGenerationExpression() {
        return generationExpression;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Boolean getMultiMap() {
        return multiMap;
    }
}
