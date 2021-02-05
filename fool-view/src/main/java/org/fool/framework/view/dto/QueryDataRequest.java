package org.fool.framework.view.dto;


import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dto.CommonRequest;
import lombok.Data;

import java.util.Map;

@Data
public class QueryDataRequest extends CommonRequest {
    private String viewName;
    private PageNavigator pageInfo;
    private Map<String,QueryValue> filter;
}
