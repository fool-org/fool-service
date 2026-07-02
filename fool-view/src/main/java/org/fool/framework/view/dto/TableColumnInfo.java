package org.fool.framework.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fool.framework.common.PropertyType;
import org.fool.framework.view.model.ItemEditType;

@Data
@ApiModel("表头列")
@Builder
public class TableColumnInfo {
    @ApiModelProperty("legacy 列ID")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("属性")
    private String property;
    @ApiModelProperty("属性名称")
    private String propertyName;
    @ApiModelProperty("显示顺序")
    private Integer showIndex;
    @ApiModelProperty("列宽")
    private Integer width;
    @ApiModelProperty("格式")
    private String format;
    @ApiModelProperty("是否只读")
    private Boolean isReadOnly;
    @ApiModelProperty("编辑类型")
    private ItemEditType editType;
    @ApiModelProperty("legacy 关联列表视图ID")
    private Long listViewId;
    @ApiModelProperty("legacy 关联列表视图类型")
    private Integer listViewType;
    @ApiModelProperty("legacy 属性类型")
    private PropertyType propertyType;
    @ApiModelProperty("legacy 属性模型ID")
    private Long propertyModel;
    @ApiModelProperty("legacy 视图项模板文件")
    private String viewFile;
}
