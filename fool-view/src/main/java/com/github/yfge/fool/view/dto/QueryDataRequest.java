package com.github.yfge.fool.view.dto;


import com.github.yfge.fool.dto.CommonRequest;
import lombok.Data;

import java.util.Map;

@Data
public class QueryDataRequest extends CommonRequest {
    private String viewName;
    private PageInfo pageInfo;
    private Map<String,QueryValue> filter;
}
