package org.fool.framework.view.dto;


import lombok.Data;
import org.fool.framework.dto.CommonRequest;


@Data
public class ViewDataRequest extends CommonRequest {
    private String viewName;
}
