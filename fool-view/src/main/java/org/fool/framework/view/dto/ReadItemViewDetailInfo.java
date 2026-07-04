package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReadItemViewDetailInfo extends ReadItemViewItemInfo {
    private List<ReadItemViewItemInfo> items;

    @JsonProperty("Items")
    public List<ReadItemViewItemInfo> getLegacyItems() {
        return items;
    }
}
