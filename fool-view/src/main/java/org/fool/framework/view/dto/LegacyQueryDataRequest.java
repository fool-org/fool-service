package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyQueryDataRequest extends CommonRequest {
    @JsonAlias({"ViewId", "viewid"})
    private Long viewId;
    @JsonAlias({"PageSize", "pagesize"})
    private Integer pageSize;
    @JsonAlias({"PageIndex", "page"})
    private Integer pageIndex;
    @JsonAlias({"OrderByItem", "orderitem"})
    private Integer orderByItem;
    @JsonAlias({"OrderByType", "ordertype"})
    private Integer orderByType;
    @JsonAlias({"QueryFilter", "filter"})
    private String queryFilter;
    private String keyword;
}
