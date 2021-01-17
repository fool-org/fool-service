package com.github.yfge.fool.auth.business.model;


import com.github.yfge.fool.common.annotation.Id;
import com.github.yfge.fool.common.annotation.Table;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Table("auth_item")
@Data
@ApiModel("用户权限")
public class Auth {
    @Id
    @ApiModelProperty("编号")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("类型 1-跳转到视图 2-父级菜单")
    private int authType;
    @ApiModelProperty("权限类型  视图名称")
    private String authName;
}
