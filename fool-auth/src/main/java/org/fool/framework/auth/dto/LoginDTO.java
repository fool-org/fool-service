package org.fool.framework.auth.dto;

import lombok.Data;

@Data
public class LoginDTO {
    private String userId;
    private String password;
}
