package org.fool.framework.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class CommonRequest {
    @JsonAlias("Token")
    private String token;
}
