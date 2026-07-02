package org.fool.framework.auth.foolframework.auth;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_DEPARTMENT", columnPrefix = "APP_DEP_")
@Data
public class Department {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column(value = "APP_DEP_ID", key = true, generationType = GenerationType.ON_INSERT)
    private Long depId;
    @Column("APP_DEP_NAME")
    private String departmentName;
    @Column("APP_DEP_DEFAULTVIEW")
    private Long defaultView;
}
