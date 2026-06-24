package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Table("SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE")
@Data
public class AuthRoleMenuItemRelation {
    @Column("SW_APP_AUTH_MENU_ID")
    private Long menuId;
    @Column("SW_APP_AUTH_ROLE_ID")
    private Long roleId;

    public static AuthRoleMenuItemRelation of(Long roleId, Long menuId) {
        AuthRoleMenuItemRelation relation = new AuthRoleMenuItemRelation();
        relation.setRoleId(roleId);
        relation.setMenuId(menuId);
        return relation;
    }
}
