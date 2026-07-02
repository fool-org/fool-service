package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_DEPARTMENT", columnPrefix = "APP_DEP_")
@Data
public class AuthDepartment {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("APP_DEP_ID")
    private Long departmentId;
    @Column("SW_APP_AUTH_COMPANY_DepsAPP_COR_ID")
    private Long ownerCompanyId;
    @Column("APP_DEP_NAME")
    private String name;
    @Column("APP_DEP_DEFAULTVIEW")
    private Long defaultViewId;
}
