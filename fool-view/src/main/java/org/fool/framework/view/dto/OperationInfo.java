package org.fool.framework.view.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.view.model.ViewOperationType;

@Data
@ApiModel("可以进行操作")
public class OperationInfo {
    @ApiModelProperty("操作名称")
    private String text;
    @ApiModelProperty("操作类型")
    private ViewOperationType type;
    @ApiModelProperty("操作视图")
    private String viewName;
}
