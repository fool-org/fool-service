package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ListDataItem {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("legacy row index")
    private Long rowIndex;
    @ApiModelProperty("结果值,为属性-值的对，值大多数情况为String(退已经格式化完成）")
    private Map<String, Object> values;
    @ApiModelProperty("legacy 结果值")
    private List<ListDataValue> items;
    @ApiModelProperty("行格式")
    private String rowFmt;
    @ApiModelProperty("可以进行的操作")
    private List<OperationInfo> operation;

    @JsonProperty("Id")
    public String getLegacyId() {
        return id;
    }

    @JsonProperty("RowIndex")
    public Long getLegacyRowIndex() {
        return rowIndex;
    }

    @JsonProperty("Items")
    public List<ListDataValue> getLegacyItems() {
        return items;
    }

    @JsonProperty("RowFmt")
    public String getLegacyRowFmt() {
        return rowFmt;
    }
}
