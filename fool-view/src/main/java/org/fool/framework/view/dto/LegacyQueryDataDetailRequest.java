package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;

@Data
public class LegacyQueryDataDetailRequest extends CommonRequest {
    @JsonAlias("ViewId")
    private Long viewId;
    @JsonAlias("ObjId")
    private Object objId;
    @JsonAlias("IdExp")
    private String idExp;
}
