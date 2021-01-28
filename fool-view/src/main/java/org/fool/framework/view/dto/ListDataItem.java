package org.fool.framework.view.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ListDataItem {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("结果值,为属性-值的对，值大多数情况为String(退已经格式化完成）")
    private Map<String, Object> values;
    @ApiModelProperty("可以进行的操作")
    private List<OperationInfo> operation;
}
