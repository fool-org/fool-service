package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_OPERATION")
@Data
public class AppInstalledOperation {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long operationId;
    @Column("SW_SYS_MODEL_OperationsMODEL_ID")
    private Long ownerModelId;
    @Column("SW_MODEL_OPERATION_NAME")
    private String name;
    @Column("SW_MODEL_OPERATION_FILTER")
    private String filter;
    @Column("SW_MODEL_OPERATION_BASETYPE")
    private Integer baseType;
    @Column("SW_MODEL_OPERATION_ARGMODEL")
    private Long argModelId;
    @Column("SW_MODEL_OPERATION_ARGFILTER")
    private String argFilter;
    @Column("SW_MODEL_OPERATION_INVOKEDLL")
    private String invokeDll;
    @Column("SW_MODEL_OPERATION_INVOKECLASS")
    private String invokeClass;
    @Column("SW_MODEL_OPERATION_INVOKEMETHOD")
    private String invokeMethod;
    @Column("SW_MODEL_OPERATION_RETURNMODEL")
    private Long returnModelId;
}
