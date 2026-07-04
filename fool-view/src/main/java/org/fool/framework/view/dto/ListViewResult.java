package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fool.framework.dao.PageNavigatorResult;

import java.time.LocalDateTime;
import java.util.List;


@Data
@ApiModel("页面查询的结果")
public class ListViewResult {
    @ApiModelProperty("翻页信息")
    private PageNavigatorResult pageInfo;
    @ApiModelProperty("legacy 总条数")
    private Long totalItem;
    @ApiModelProperty("legacy 总页数")
    private Long totalPage;
    @ApiModelProperty("legacy 当前页")
    private Long pageIndex;
    @ApiModelProperty("列名")
    private List<String> cols;
    @ApiModelProperty("刷新时间")
    private LocalDateTime freshTime;
    @ApiModelProperty("自动刷新间隔")
    private Integer autoFreshTime;
    @ApiModelProperty("结果数据")
    private List<ListDataItem> items;
    @ApiModelProperty("legacy 结果数据")
    private List<ListDataItem> data;

    @JsonProperty("TotalItem")
    public Long getLegacyTotalItem() {
        return totalItem;
    }

    @JsonProperty("TotalPage")
    public Long getLegacyTotalPage() {
        return totalPage;
    }

    @JsonProperty("PageIndex")
    public Long getLegacyPageIndex() {
        return pageIndex;
    }

    @JsonProperty("Cols")
    public List<String> getLegacyCols() {
        return cols;
    }

    @JsonProperty("FreshTime")
    public LocalDateTime getLegacyFreshTime() {
        return freshTime;
    }

    @JsonProperty("AutoFreshTime")
    public Integer getLegacyAutoFreshTime() {
        return autoFreshTime;
    }

    @JsonProperty("Data")
    public List<ListDataItem> getLegacyData() {
        return data;
    }
}
