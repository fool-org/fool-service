package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class InputQueryRequest extends CommonRequest {
    private String text;
    private String viewItemId;
    private String viewName;
    private String modelID;
    private String objID;
    private boolean isAdded;
    private String ownerId;
}
