package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class GetEnumRequest extends CommonRequest {
    @JsonAlias({"ModelId", "ModelID", "modelid"})
    private String modelId;
}
