package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Table("SW_APP_AUTH_MENU_SubItems")
@Data
public class AuthMenuSubItemRelation {
    @Column("SW_APP_AUTH_MENU_SubItemsAUTH_MENU_ID")
    private Long parentMenuId;
    @Column("SW_APP_AUTH_MENU_SUBITEMS_ITEM")
    private Long subItemMenuId;

    public static AuthMenuSubItemRelation of(Long parentMenuId, Long subItemMenuId) {
        AuthMenuSubItemRelation relation = new AuthMenuSubItemRelation();
        relation.setParentMenuId(parentMenuId);
        relation.setSubItemMenuId(subItemMenuId);
        return relation;
    }
}
