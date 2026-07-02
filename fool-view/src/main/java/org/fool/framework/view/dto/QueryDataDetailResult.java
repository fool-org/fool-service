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
        private List<Object> items;
        private String model;
        private String parentId;
    }
}
