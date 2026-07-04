package org.fool.framework.view.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.view.model.ViewOperationType;

import java.util.List;

@Data
@ApiModel("可以进行操作")
public class OperationInfo {
    @ApiModelProperty("操作ID")
    private Long id;
    @ApiModelProperty("legacy 操作名称")
    private String name;
    @ApiModelProperty("操作名称")
    private String text;
    @ApiModelProperty("操作类型")
    private ViewOperationType type;
    @ApiModelProperty("操作视图")
    private String viewName;
    @ApiModelProperty("操作视图ID")
    private Long viewId;
    @ApiModelProperty("是否需要选择数据")
    private boolean requireSelect;
    @ApiModelProperty("操作位置")
    private int location;
    @ApiModelProperty("legacy 操作参数")
    private List<OperationParamInfo> params;

    @JsonProperty("ID")
    public Long getLegacyId() {
        return id;
    }

    @JsonProperty("Name")
    public String getLegacyName() {
        return name;
    }

    @JsonProperty("RequireSelect")
    public boolean getLegacyRequireSelect() {
        return requireSelect;
    }

    @JsonProperty("ViewID")
    public Long getLegacyViewId() {
        return viewId;
    }
}
