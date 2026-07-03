package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class QueryDataDetailResult {
    private DataDetail data;
    private Integer autoFreshTime;
    private Boolean canEdit;
    private List<OperationInfo> operations;

    @JsonProperty("Data")
    public DataDetail getLegacyData() {
        return data;
    }

    @JsonProperty("AutoFreshTime")
    public Integer getLegacyAutoFreshTime() {
        return autoFreshTime;
    }

    @JsonProperty("CanEdit")
    public Boolean getLegacyCanEdit() {
        return canEdit;
    }

    @JsonProperty("Operations")
    public List<OperationInfo> getLegacyOperations() {
        return operations;
    }

    @Data
    public static class DataDetail {
        private String objId;
        private String name;
        private List<ListDataValue> simpleData;
        private List<PropertyDataItems> items;
        private String model;
        private String parentId;

        @JsonProperty("ObjId")
        public String getLegacyObjId() {
            return objId;
        }

        @JsonProperty("Name")
        public String getLegacyName() {
            return name;
        }

        @JsonProperty("SimpleData")
        public List<ListDataValue> getLegacySimpleData() {
            return simpleData;
        }

        @JsonProperty("Items")
        public List<PropertyDataItems> getLegacyItems() {
            return items;
        }

        @JsonProperty("Model")
        public String getLegacyModel() {
            return model;
        }

        @JsonProperty("ParentId")
        public String getLegacyParentId() {
            return parentId;
        }
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

        @JsonProperty("Properties")
        public List<ListDataValue> getLegacyProperties() {
            return properties;
        }

        @JsonProperty("Items")
        public List<DataItem> getLegacyItems() {
            return items;
        }

        @JsonProperty("ListViewId")
        public Long getLegacyListViewId() {
            return listViewId;
        }

        @JsonProperty("DetailViewId")
        public Long getLegacyDetailViewId() {
            return detailViewId;
        }

        @JsonProperty("Name")
        public String getLegacyName() {
            return name;
        }

        @JsonProperty("PrpId")
        public String getLegacyPrpId() {
            return prpId;
        }

        @JsonProperty("SelectFromExists")
        public Boolean getLegacySelectFromExists() {
            return selectFromExists;
        }

        @JsonProperty("ItemName")
        public String getLegacyItemName() {
            return itemName;
        }

        @JsonProperty("SelectedView")
        public Long getLegacySelectedView() {
            return selectedView;
        }
    }

    @Data
    public static class DataItem {
        private String dataId;
        private List<ListDataValue> values;

        @JsonProperty("DataID")
        public String getLegacyDataId() {
            return dataId;
        }

        @JsonProperty("Values")
        public List<ListDataValue> getLegacyValues() {
            return values;
        }
    }
}
