package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.common.PropertyType;
import org.fool.framework.view.model.ItemEditType;

@Data
public class ListDataValue {
    @ApiModelProperty("legacy 值对象ID")
    private String objId;
    @ApiModelProperty("legacy 属性ID")
    private String prpId;
    @ApiModelProperty("legacy 格式化值")
    private String fmtValue;
    @ApiModelProperty("legacy 属性显示名")
    private String prpShowName;
    @ApiModelProperty("legacy 属性类型")
    private PropertyType prpType;
    @ApiModelProperty("legacy 属性模型ID")
    private Long prpModelId;
    @ApiModelProperty("legacy 是否只读")
    private Boolean readOnly;
    @ApiModelProperty("legacy 编辑类型")
    private ItemEditType editType;

    @JsonProperty("ObjId")
    public String getLegacyObjId() {
        return objId;
    }

    @JsonProperty("PrpId")
    public String getLegacyPrpId() {
        return prpId;
    }

    @JsonProperty("FmtValue")
    public String getLegacyFmtValue() {
        return fmtValue;
    }

    @JsonProperty("PrpShowName")
    public String getLegacyPrpShowName() {
        return prpShowName;
    }

    @JsonProperty("Name")
    public String getLegacyName() {
        return prpShowName;
    }

    @JsonProperty("PrpType")
    public PropertyType getLegacyPrpType() {
        return prpType;
    }

    @JsonProperty("PrpModelId")
    public Long getLegacyPrpModelId() {
        return prpModelId;
    }

    @JsonProperty("ReadOnly")
    public Boolean getLegacyReadOnly() {
        return readOnly;
    }

    @JsonProperty("EditType")
    public ItemEditType getLegacyEditType() {
        return editType;
    }
}
