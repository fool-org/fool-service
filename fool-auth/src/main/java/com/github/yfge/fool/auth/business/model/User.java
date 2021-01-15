package com.github.yfge.fool.auth.business.model;


import com.github.yfge.fool.common.annotation.Id;
import com.github.yfge.fool.common.annotation.SqlGenerate;
import com.github.yfge.fool.common.annotation.SqlGenerateConfig;
import com.github.yfge.fool.common.annotation.Table;
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
