package org.fool.framework.agent.service;

public class ReportQueryMetadataColumn {
    private final Long viewItemId;
    private final String itemName;
    private final String itemLabel;
    private final String modelProperty;
    private final Integer showIndex;
    private final Boolean canEdit;
    private final Integer inputType;
    private final Integer editType;
    private final Integer width;
    private final String sourceExpression;
    private final Long listViewId;
    private final Long editViewId;
    private final Long selectedViewId;
    private final Long propertyId;
    private final String propertyName;
    private final String propertyRemark;
    private final String dbColumn;
    private final Integer propertyType;
    private final Boolean collection;

    public ReportQueryMetadataColumn(Long viewItemId,
                                     String itemName,
                                     String itemLabel,
                                     String modelProperty,
                                     Integer showIndex,
                                     Boolean canEdit,
                                     Integer editType,
                                     Long listViewId,
                                     Long editViewId,
                                     Long selectedViewId,
                                     Long propertyId,
                                     String propertyName,
                                     String propertyRemark,
                                     String dbColumn,
                                     Integer propertyType,
                                     Boolean collection) {
        this(viewItemId,
                itemName,
                itemLabel,
                modelProperty,
                showIndex,
                canEdit,
                null,
                editType,
                null,
                null,
                listViewId,
                editViewId,
                selectedViewId,
                propertyId,
                propertyName,
                propertyRemark,
                dbColumn,
                propertyType,
                collection);
    }

    public ReportQueryMetadataColumn(Long viewItemId,
                                     String itemName,
                                     String itemLabel,
                                     String modelProperty,
                                     Integer showIndex,
                                     Boolean canEdit,
                                     Integer inputType,
                                     Integer editType,
                                     Integer width,
                                     String sourceExpression,
                                     Long listViewId,
                                     Long editViewId,
                                     Long selectedViewId,
                                     Long propertyId,
                                     String propertyName,
                                     String propertyRemark,
                                     String dbColumn,
                                     Integer propertyType,
                                     Boolean collection) {
        this.viewItemId = viewItemId;
        this.itemName = itemName;
        this.itemLabel = itemLabel;
        this.modelProperty = modelProperty;
        this.showIndex = showIndex;
        this.canEdit = canEdit;
        this.inputType = inputType;
        this.editType = editType;
        this.width = width;
        this.sourceExpression = sourceExpression;
        this.listViewId = listViewId;
        this.editViewId = editViewId;
        this.selectedViewId = selectedViewId;
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.propertyRemark = propertyRemark;
        this.dbColumn = dbColumn;
        this.propertyType = propertyType;
        this.collection = collection;
    }

    public Long getViewItemId() {
        return viewItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public String getModelProperty() {
        return modelProperty;
    }

    public Integer getShowIndex() {
        return showIndex;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public Integer getInputType() {
        return inputType;
    }

    public Integer getEditType() {
        return editType;
    }

    public Integer getWidth() {
        return width;
    }

    public String getSourceExpression() {
        return sourceExpression;
    }

    public Long getListViewId() {
        return listViewId;
    }

    public Long getEditViewId() {
        return editViewId;
    }

    public Long getSelectedViewId() {
        return selectedViewId;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyRemark() {
        return propertyRemark;
    }

    public String getDbColumn() {
        return dbColumn;
    }

    public Integer getPropertyType() {
        return propertyType;
    }

    public Boolean getCollection() {
        return collection;
    }

    public boolean isReportable() {
        return propertyId != null && propertyType != null && !Boolean.TRUE.equals(collection);
    }

    public boolean isChildCollection() {
        return Boolean.TRUE.equals(collection)
                || listViewId != null
                || editViewId != null
                || selectedViewId != null;
    }

    public String reportColumnId() {
        if (hasText(propertyName)) {
            return propertyName;
        }
        if (hasText(modelProperty)) {
            return modelProperty;
        }
        return propertyId == null ? null : propertyId.toString();
    }

    public String displayName() {
        if (hasText(itemName)) {
            return itemName;
        }
        if (hasText(itemLabel)) {
            return itemLabel;
        }
        if (hasText(propertyRemark)) {
            return propertyRemark;
        }
        return reportColumnId();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
