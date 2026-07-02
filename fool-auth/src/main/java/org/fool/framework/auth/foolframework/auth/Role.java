package org.fool.framework.auth.foolframework.auth;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Table(value = "SW_APP_AUTH_ROLE", columnPrefix = "AUTH_ROLE_")
@Data
public class Role {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column(value = "AUTH_ROLE_ID", key = true, generationType = GenerationType.ON_INSERT)
    private Long roleId;
    @Column("AUTH_ROLE_NAME")
    private String roleName;
    private transient List<AuthorizedUser> authUsers = new ArrayList<>();
    private transient List<Department> authDeps = new ArrayList<>();
    private transient List<MenuItem> items = new ArrayList<>();
}
