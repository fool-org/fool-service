package org.fool.framework.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("表头列")
@Builder
public class TableColumnInfo {
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("属性")
    private String property;
}
