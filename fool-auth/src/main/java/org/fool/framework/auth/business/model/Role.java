package org.fool.framework.auth.business.model;


import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("auth_role")
public class Role {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    private String id;
    private String name;
}
