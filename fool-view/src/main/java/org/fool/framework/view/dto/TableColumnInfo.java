package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
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
    @ApiModelProperty("legacy 属性ID")
    private Long propertyId;
    @ApiModelProperty("legacy 关联列表视图ID")
    private Long listViewId;
    @ApiModelProperty("legacy 关联列表视图类型")
    private Integer listViewType;
    @ApiModelProperty("legacy 编辑视图ID")
    private Long editViewId;
    @ApiModelProperty("legacy 编辑表达式")
    private Long editExp;
    @ApiModelProperty("legacy 属性类型")
    private PropertyType propertyType;
    @ApiModelProperty("legacy 属性模型ID")
    private Long propertyModel;
    @ApiModelProperty("legacy 视图项模板文件")
    private String viewFile;

    @JsonProperty("ID")
    public Long getLegacyId() {
        return id;
    }

    @JsonProperty("Name")
    public String getLegacyName() {
        return name;
    }

    @JsonProperty("Format")
    public String getLegacyFormat() {
        return format;
    }

    @JsonProperty("IsReadOnly")
    public Boolean getLegacyIsReadOnly() {
        return isReadOnly;
    }

    @JsonProperty("ShowIndex")
    public Integer getLegacyShowIndex() {
        return showIndex;
    }

    @JsonProperty("Width")
    public Integer getLegacyWidth() {
        return width;
    }

    @JsonProperty("PropertyName")
    public String getLegacyPropertyName() {
        return propertyName;
    }

    @JsonProperty("PropertyId")
    public Long getLegacyPropertyId() {
        return propertyId;
    }

    @JsonProperty("ListViewType")
    public Integer getLegacyListViewType() {
        return listViewType;
    }

    @JsonProperty("ListViewId")
    public Long getLegacyListViewId() {
        return listViewId;
    }

    @JsonProperty("EditViewId")
    public Long getLegacyEditViewId() {
        return editViewId;
    }

    @JsonProperty("EditExp")
    public Long getLegacyEditExp() {
        return editExp;
    }

    @JsonProperty("ViewFile")
    public String getLegacyViewFile() {
        return viewFile;
    }

    @JsonProperty("PropertyType")
    public PropertyType getLegacyPropertyType() {
        return propertyType;
    }

    @JsonProperty("PropertyModel")
    public Long getLegacyPropertyModel() {
        return propertyModel;
    }

    @JsonProperty("EditType")
    public ItemEditType getLegacyEditType() {
        return editType;
    }
}
