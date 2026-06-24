package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Table("SW_APP_AUTH_ROLE_SW_APP_AUTH_USER")
@Data
public class AuthRoleAuthorizedUserRelation {
    @Column("SW_APP_AUTH_ROLE_ID")
    private Long roleId;
    @Column("SW_APP_AUTH_USER_ID")
    private Long authorizedUserId;

    public static AuthRoleAuthorizedUserRelation of(Long roleId, Long authorizedUserId) {
        AuthRoleAuthorizedUserRelation relation = new AuthRoleAuthorizedUserRelation();
        relation.setRoleId(roleId);
        relation.setAuthorizedUserId(authorizedUserId);
        return relation;
    }
}
