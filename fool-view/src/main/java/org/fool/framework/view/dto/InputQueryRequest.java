package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class InputQueryRequest extends CommonRequest {
    @JsonAlias({"Text", "text"})
    private String text;
    @JsonAlias({"ViewItemId", "itemid"})
    private String viewItemId;
    @JsonAlias("ViewId")
    private Long viewId;
    @JsonAlias({"ViewName", "viewid"})
    private String viewName;
    @JsonAlias({"ModelID", "modelid"})
    private String modelID;
    @JsonAlias({"ObjID", "objid"})
    private String objID;
    @JsonAlias({"IsAdded", "isAdded", "newadd"})
    private boolean isAdded;
    @JsonAlias({"OwnerId", "ownerid"})
    private String ownerId;
}
