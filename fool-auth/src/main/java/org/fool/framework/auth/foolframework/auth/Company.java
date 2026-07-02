package org.fool.framework.auth.foolframework.auth;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table(value = "SW_APP_AUTH_COMPANY", columnPrefix = "APP_COR_")
@Data
public class Company {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column(value = "APP_COR_ID", key = true, generationType = GenerationType.ON_INSERT)
    private Long id;
    @Column("APP_COR_NAME")
    private String name;
}
