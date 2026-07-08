package org.fool.framework.view.dto;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dto.CommonRequest;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryDataRequest extends CommonRequest {
    @JsonAlias("ViewId")
    private Long viewId;
    private PageNavigator pageInfo;
    private Map<String,QueryValue> filter;
    private String keyword;
}
