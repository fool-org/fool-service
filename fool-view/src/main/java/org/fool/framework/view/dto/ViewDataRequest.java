package org.fool.framework.view.dto;


import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;


@Data
public class ViewDataRequest extends CommonRequest {
    @JsonAlias("ViewId")
    private Long viewId;
    private String viewName;
}
