package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyQueryDataRequest extends CommonRequest {
    private Long viewId;
    private Integer pageSize;
    private Integer pageIndex;
    private Integer orderByItem;
    private Integer orderByType;
    private String queryFilter;
    private String keyword;
}
