package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacySaveNewObjRequest extends CommonRequest {
    @JsonAlias({"SaveObj", "obj"})
    private SaveObjRequest.SaveObject saveObj;
    @JsonAlias({"OwnerViewId", "ownerviewid"})
    private String ownerViewId;
    @JsonAlias({"OwnerId", "ownerid"})
    private String ownerId;
    @JsonAlias({"Property", "prpid"})
    private String property;
}
