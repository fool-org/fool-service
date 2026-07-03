package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyQueryDataRequest extends CommonRequest {
    @JsonAlias("ViewId")
    private Long viewId;
    @JsonAlias("PageSize")
    private Integer pageSize;
    @JsonAlias("PageIndex")
    private Integer pageIndex;
    @JsonAlias("OrderByItem")
    private Integer orderByItem;
    @JsonAlias("OrderByType")
    private Integer orderByType;
    @JsonAlias("QueryFilter")
    private String queryFilter;
    private String keyword;
}
