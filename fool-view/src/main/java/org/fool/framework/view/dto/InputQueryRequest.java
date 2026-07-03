package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class InputQueryRequest extends CommonRequest {
    @JsonAlias("Text")
    private String text;
    @JsonAlias("ViewItemId")
    private String viewItemId;
    @JsonAlias("ViewId")
    private Long viewId;
    @JsonAlias("ViewName")
    private String viewName;
    @JsonAlias("ModelID")
    private String modelID;
    @JsonAlias("ObjID")
    private String objID;
    @JsonAlias({"IsAdded", "isAdded"})
    private boolean isAdded;
    @JsonAlias("OwnerId")
    private String ownerId;
}
