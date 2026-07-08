package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @ApiModelProperty("legacy 类型")
    private ViewType type;
    @ApiModelProperty("legacy 显示类型")
    private ViewType showType;
    @ApiModelProperty("legacy 模板文件")
    private String tempFile;
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

    @JsonProperty("ID")
    public Long getLegacyId() {
        return id;
    }

    @JsonProperty("ViewId")
    public Long getLegacyViewId() {
        return id;
    }

    @JsonProperty("Name")
    public String getLegacyName() {
        return name;
    }

    @JsonProperty("Type")
    public ViewType getLegacyType() {
        return type;
    }

    @JsonProperty("Items")
    public List<TableColumnInfo> getLegacyItems() {
        return tableColumn;
    }

    @JsonProperty("Operations")
    public List<OperationInfo> getLegacyOperations() {
        return operations;
    }

    @JsonProperty("DetailViewId")
    public Long getLegacyDetailViewId() {
        return detailViewId;
    }

    @JsonProperty("TempFile")
    public String getLegacyTempFile() {
        return tempFile;
    }

    @JsonProperty("ShowType")
    public ViewType getLegacyShowType() {
        return showType;
    }

    @JsonProperty("AutoFreshTime")
    public Integer getLegacyAutoFreshTime() {
        return autoFreshTime;
    }
}
