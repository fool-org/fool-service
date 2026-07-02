package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class GetEnumRequest extends CommonRequest {
    private String modelId;
}
