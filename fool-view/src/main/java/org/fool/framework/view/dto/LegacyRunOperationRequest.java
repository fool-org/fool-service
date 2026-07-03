package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyRunOperationRequest extends CommonRequest {
    @JsonAlias("ObjectId")
    private String objectId;
    @JsonAlias("OperationId")
    private Long operationId;
    @JsonAlias("ViewId")
    private Long viewId;
}
