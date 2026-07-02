package org.fool.framework.view.dto;

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
}
