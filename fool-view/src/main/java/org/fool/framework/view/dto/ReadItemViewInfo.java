package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReadItemViewInfo {
    private String viewName;
    private Long viewId;
    private List<ReadItemViewItemInfo> items;
    private List<ReadItemViewDetailInfo> detailViews;

    @JsonProperty("ViewName")
    public String getLegacyViewName() {
        return viewName;
    }

    @JsonProperty("ViewId")
    public Long getLegacyViewId() {
        return viewId;
    }

    @JsonProperty("Items")
    public List<ReadItemViewItemInfo> getLegacyItems() {
        return items;
    }

    @JsonProperty("DetailViews")
    public List<ReadItemViewDetailInfo> getLegacyDetailViews() {
        return detailViews;
    }
}
