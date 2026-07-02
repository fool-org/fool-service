package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_OPERATION_PARAM")
@Data
public class AppInstalledOperationParam {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long paramId;
    @Column("SW_SYS_OPERATION_ParamsSysId")
    private Long ownerOperationId;
    @Column("SW_SYS_OPERATION_PARAM_NAME")
    private String name;
    @Column("SW_SYS_OPERATION_PARAM_VIEW")
    private Long viewId;
    @Column("SW_SYS_OPERATION_PARAM_FILTER")
    private String filter;
    @Column("SW_SYS_OPERATION_PARAM_VALUE")
    private String value;
}
