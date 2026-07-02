package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_ROLE", columnPrefix = "AUTH_ROLE_")
@Data
public class AuthRole {
    @Id
    @SqlGenerate
    @Column("AUTH_ROLE_ID")
    private Long roleId;
    @Column("AUTH_ROLE_NAME")
    private String roleName;

    public static AuthRole fromBootstrap(BootstrapRole bootstrap) {
        AuthRole role = new AuthRole();
        role.setRoleName(bootstrap.getRoleName());
        return role;
    }
}
