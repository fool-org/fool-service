package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_MODEL_TRIGGER")
@Data
public class AppInstalledModelTrigger {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long triggerId;
    @Column("SW_SYS_MODEL_TriggersMODEL_ID")
    private Long ownerModelId;
    @Column("SW_MODEL_TRIGGER_ARGMODEL")
    private Long argModelId;
    @Column("SW_MODEL_TRIGGER_TYPE")
    private Integer triggerType;
    @Column("SW_MODEL_TRIGGER_FILTER")
    private String filter;
    @Column("SW_MODEL_TRIGGER_ARGFILTER")
    private String argFilter;
    @Column("SW_MODEL_TRIGGER_OPERATIONTYPE")
    private Integer operationType;
    @Column("SW_MODEL_TRIGGER_INVOKEDLL")
    private String invokeDll;
    @Column("SW_MODEL_TRIGGER_INVOKECLASS")
    private String invokeClass;
    @Column("SW_MODEL_TRIGGER_INVOKEMETHOD")
    private String invokeMethod;
}
