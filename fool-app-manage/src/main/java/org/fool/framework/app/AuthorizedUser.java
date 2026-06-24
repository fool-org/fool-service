package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.Table;

@Table("SW_APP_AUTH_USER")
@Data
public class AuthorizedUser {
    @Id
    @SqlGenerate
    @Column("APP_AUTH_ID")
    private Long authorizedId;
    @Column("APP_AUTH_USERID")
    private String userId;
    @Column("APP_AUTH_USERLOGINNAME")
    private String userLoginName;
    @Column("APP_AUTH_DEP")
    private Long departmentId;

    public static AuthorizedUser forUser(String userId) {
        AuthorizedUser user = new AuthorizedUser();
        user.setUserId(userId);
        user.setUserLoginName(userId);
        return user;
    }
}
