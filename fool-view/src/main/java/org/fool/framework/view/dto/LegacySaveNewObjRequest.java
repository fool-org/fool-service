package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacySaveNewObjRequest extends CommonRequest {
    @JsonAlias("SaveObj")
    private SaveObjRequest.SaveObject saveObj;
    @JsonAlias("OwnerViewId")
    private String ownerViewId;
    @JsonAlias("OwnerId")
    private String ownerId;
    @JsonAlias("Property")
    private String property;
}
