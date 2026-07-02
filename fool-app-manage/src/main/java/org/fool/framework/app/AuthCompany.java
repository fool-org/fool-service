package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_COMPANY", columnPrefix = "APP_COR_")
@Data
public class AuthCompany {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("APP_COR_ID")
    private Long companyId;
    @Column("APP_COR_NAME")
    private String name;
}
