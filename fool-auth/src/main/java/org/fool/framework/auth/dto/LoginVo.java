package org.fool.framework.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("登录结果")
public class LoginVo {
    @ApiModelProperty("用户信息")
    private UserDTO user;
    @ApiModelProperty("token")
    private String token;
}
