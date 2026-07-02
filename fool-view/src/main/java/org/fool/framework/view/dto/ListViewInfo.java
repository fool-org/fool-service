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
    @ApiModelProperty("legacy 名称")
    private String name;
    @ApiModelProperty("标题")
    private String viewTitle;
    @ApiModelProperty("浏览器标题")
    private String browserTitle;
    @ApiModelProperty("类型")
    private ViewType viewType;
    @ApiModelProperty("legacy 显示类型")
    private ViewType showType;
    @ApiModelProperty("默认详情视图ID")
    private Long detailViewId;
    @ApiModelProperty("自动刷新间隔")
    private Integer autoFreshTime;
    @ApiModelProperty("查询/输入选项")
    private List<ViewInputInfo> inputInfo;
    @ApiModelProperty("表头")
    private List<TableColumnInfo> tableColumn;
    @ApiModelProperty("可执行操作")
    private List<OperationInfo> operations;
}
