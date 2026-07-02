package org.fool.framework.view.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReadItemViewInfo {
    private String viewName;
    private Long viewId;
    private List<ReadItemViewItemInfo> items;
    private List<ReadItemViewDetailInfo> detailViews;
}
