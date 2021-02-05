package org.fool.framework.view.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.dao.PageNavigatorResult;

import java.util.List;


@Data
@ApiModel("页面查询的结果")
public class ListViewResult {
    @ApiModelProperty("翻页信息")
    private PageNavigatorResult pageInfo;
    @ApiModelProperty("结果数据")
    private List<ListDataItem> items;
}
