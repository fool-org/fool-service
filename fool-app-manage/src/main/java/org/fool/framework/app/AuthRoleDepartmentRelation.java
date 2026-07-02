package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Table("SW_APP_AUTH_DEPARTMENT_SW_APP_AUTH_ROLE")
@Data
public class AuthRoleDepartmentRelation {
    @Column("SW_APP_AUTH_DEPARTMENT_ID")
    private Long departmentId;
    @Column("SW_APP_AUTH_ROLE_ID")
    private Long roleId;

    public static AuthRoleDepartmentRelation of(Long roleId, Long departmentId) {
        AuthRoleDepartmentRelation relation = new AuthRoleDepartmentRelation();
        relation.setRoleId(roleId);
        relation.setDepartmentId(departmentId);
        return relation;
    }
}
