package org.fool.framework.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.view.model.OperationViewParam;

@Data
@ApiModel("操作参数")
public class OperationParamInfo {
    @ApiModelProperty("legacy 参数项ID")
    private Long id;
    @ApiModelProperty("legacy 操作视图参数名称")
    private String name;
    @ApiModelProperty("legacy 参数顺序")
    private Integer index;
    @ApiModelProperty("legacy 操作参数ID")
    private Long paramId;
    @ApiModelProperty("legacy 操作参数名称")
    private String paramName;
    @ApiModelProperty("legacy 参数选择视图ID")
    private Long viewId;
    @ApiModelProperty("legacy 参数筛选条件")
    private String filter;
    @ApiModelProperty("legacy 参数值")
    private String value;

    public static OperationParamInfo from(OperationViewParam param) {
        OperationParamInfo result = new OperationParamInfo();
        result.setId(param.getId());
        result.setName(param.getName());
        result.setIndex(param.getIndex());
        result.setParamId(param.getParamId());
        result.setParamName(param.getParamName());
        result.setViewId(param.getViewId());
        result.setFilter(param.getFilter());
        result.setValue(param.getValue());
        return result;
    }
}
