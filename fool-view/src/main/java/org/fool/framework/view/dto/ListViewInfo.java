package org.fool.framework.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.view.model.ViewType;

import java.util.List;


@Data
@ApiModel("返回的视图定义")
public class ListViewInfo {
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("名称")
    private String viewName;
    @ApiModelProperty("标题")
    private String viewTitle;
    @ApiModelProperty("浏览器标题")
    private String browserTitle;
    @ApiModelProperty("类型")
    private ViewType viewType;
    @ApiModelProperty("查询/输入选项")
    private List<ViewInputInfo> inputInfo;
    @ApiModelProperty("表头")
    private List<TableColumnInfo> tableColumn;
}
