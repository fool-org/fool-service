package org.fool.framework.auth.foolframework.auth;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_USER", columnPrefix = "APP_AUTH_")
@Data
public class AuthorizedUser {
    @Column(value = "APP_AUTH_USERID", propertyName = "UserID")
    @Column(value = "APP_AUTH_USERLOGINNAME", propertyName = "LoginName")
    private User user;
    @Column("APP_AUTH_DEP")
    private Department department;
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column(value = "APP_AUTH_ID", key = true, generationType = GenerationType.ON_INSERT)
    private Long authorizedId;
}
