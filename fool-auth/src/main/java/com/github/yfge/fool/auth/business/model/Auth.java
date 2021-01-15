package com.github.yfge.fool.auth.business.model;


import com.github.yfge.fool.common.annotation.Id;
import com.github.yfge.fool.common.annotation.Table;
import lombok.Data;

@Table("auth_item")
@Data
public class Auth {
    @Id
    private String id;
    private String name;
    private int authType;
    private String authName;
}
