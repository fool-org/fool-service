package org.fool.framework.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户信息")
public class UserDTO {
    @ApiModelProperty("登录id")
    private String id;
    @ApiModelProperty("姓名")
    private String name;
    @ApiModelProperty("手机")
    private String mobile;

}
