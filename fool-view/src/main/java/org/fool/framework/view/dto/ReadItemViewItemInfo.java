package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.fool.framework.common.PropertyType;
import org.fool.framework.view.model.ItemEditType;

@Data
public class ReadItemViewItemInfo {
    private String name;
    private PropertyType prpType;
    private Integer index;
    private String prpId;
    private Long prpModelId;
    private String id;
    private String prpShowName;
    private boolean readOnly;
    private ItemEditType editType;

    @JsonProperty("Name")
    public String getLegacyName() {
        return name;
    }

    @JsonProperty("PrpType")
    public PropertyType getLegacyPrpType() {
        return prpType;
    }

    @JsonProperty("Index")
    public Integer getLegacyIndex() {
        return index;
    }

    @JsonProperty("PrpId")
    public String getLegacyPrpId() {
        return prpId;
    }

    @JsonProperty("PrpModelId")
    public Long getLegacyPrpModelId() {
        return prpModelId;
    }

    @JsonProperty("ID")
    public String getLegacyId() {
        return id;
    }

    @JsonProperty("PrpShowName")
    public String getLegacyPrpShowName() {
        return prpShowName;
    }

    @JsonProperty("ReadOnly")
    public boolean getLegacyReadOnly() {
        return readOnly;
    }

    @JsonProperty("EditType")
    public ItemEditType getLegacyEditType() {
        return editType;
    }
}
