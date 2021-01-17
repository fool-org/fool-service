package org.fool.framework.auth.business.model;


import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Table("auth_user")
@Data
public class User {
    @Id
    private String id;
    private String mobile;
    private String name;
    private String password;
    @SqlGenerate(SqlGenerateConfig.INSERT)
    private LocalDateTime createdAt;
    @SqlGenerate(SqlGenerateConfig.INSERT)
    private LocalDateTime lastLogin;
}
