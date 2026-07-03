package org.fool.framework.view.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueryDataDetailResult {
    private DataDetail data;
    private Integer autoFreshTime;
    private Boolean canEdit;
    private List<OperationInfo> operations;

    @Data
    public static class DataDetail {
        private String objId;
        private String name;
        private List<ListDataValue> simpleData;
        private List<PropertyDataItems> items;
        private String model;
        private String parentId;
    }

    @Data
    public static class PropertyDataItems {
        private List<ListDataValue> properties;
        private List<DataItem> items;
        private Long listViewId;
        private Long detailViewId;
        private String name;
        private String prpId;
        private Boolean selectFromExists;
        private String itemName;
        private Long selectedView;
    }

    @Data
    public static class DataItem {
        private String dataId;
        private List<ListDataValue> values;
    }
}
