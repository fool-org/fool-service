package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyQueryDataDetailRequest extends CommonRequest {
    private Long viewId;
    private Object objId;
    @JsonAlias("IdExp")
    private String idExp;
}
