package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyRunOperationRequest extends CommonRequest {
    @JsonAlias({"ObjectId", "objid"})
    private String objectId;
    @JsonAlias({"OperationId", "opid"})
    private Long operationId;
    @JsonAlias({"ViewId", "viewid"})
    private Long viewId;
}
