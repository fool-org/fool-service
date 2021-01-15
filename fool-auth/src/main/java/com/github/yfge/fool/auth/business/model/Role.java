package com.github.yfge.fool.auth.business.model;


import com.github.yfge.fool.common.annotation.SqlGenerate;
import com.github.yfge.fool.common.annotation.SqlGenerateConfig;
import com.github.yfge.fool.common.annotation.Table;

@Table("auth_role")
public class Role {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    private String id;
    private String name;
}
