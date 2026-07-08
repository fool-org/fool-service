package org.fool.framework.view.dto;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.fool.framework.dto.CommonRequest;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViewDataRequest extends CommonRequest {
    @JsonAlias("ViewId")
    private Long viewId;
}
