package org.fool.framework.auth.foolframework.auth;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.EncryptType;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

import java.time.LocalDateTime;

@Table(value = "SW_AUTH_USER", columnPrefix = "USER_")
@Data
public class User {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column(value = "USER_UID", key = true, generationType = GenerationType.ON_INSERT)
    private Long userId;
    @Column(value = "USER_UUID", key = true, keyGroupName = "UUID")
    private String userGuid;
    @Column(value = "USER_LOGINNAME", key = true, keyGroupName = "LOGINNAME")
    private String loginName;
    @Column(value = "USER_PHONE", key = true, keyGroupName = "PHONE")
    private String phone;
    @Column(value = "USER_MAIL", key = true, keyGroupName = "MAIL")
    private String email;
    @Column("USER_FIRSTNAME")
    private String firstName;
    @Column("USER_LASTNAME")
    private String lastName;
    @Column("USER_SHOWNAME")
    private String showName;
    @Column("USER_TITLE")
    private String title;
    @Column("USER_AVTAR")
    private String avtar;
    @Column(value = "USER_PWD", encryptType = EncryptType.MD5)
    private String password;
    @Column(value = "USER_REGTIME", generationType = GenerationType.ON_INSERT_AND_UPDATE)
    private LocalDateTime createTime;
    @Column("USER_LASTLOGINTIME")
    private LocalDateTime lastLoginTime;
    @Column("USER_LASTMODIFYTIME")
    private LocalDateTime lastModifyTime;
    @Column("USER_SEX")
    private Sex sex;
    @Column("USER_DEFAULTVIEW")
    private Long defaultViewId;
}
