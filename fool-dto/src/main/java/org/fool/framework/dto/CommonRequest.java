package org.fool.framework.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"token", "Token"})
public class CommonRequest {
}
