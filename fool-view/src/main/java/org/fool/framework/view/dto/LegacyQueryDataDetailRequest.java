package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyQueryDataDetailRequest extends CommonRequest {
    private Long viewId;
    private Object objId;
    private String idExp;
}
