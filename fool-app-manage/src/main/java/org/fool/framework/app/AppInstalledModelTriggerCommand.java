package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_MODEL_TRIGGER_COMMANDS")
@Data
public class AppInstalledModelTriggerCommand {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long commandId;
    @Column("SW_SYS_MODEL_TRIGGER_CommandsSysId")
    private Long ownerTriggerId;
    @Column("SW_SYS_COMMAND_TYPE")
    private Integer commandType;
    @Column("SW_SYS_COMMAND_PROPERTY")
    private Long propertyId;
    @Column("SW_SYS_COMMAND_EXP")
    private String expression;
    @Column("SW_SYS_COMMAND_ARGMODEL")
    private Long argModelId;
    @Column("SW_SYS_COMMAND_ARGEXP")
    private String argExpression;
    @Column("SW_SYS_COMMAND_ARGID")
    private String argSourceIdExpression;
    @Column("SW_SYS_COMMAND_Index")
    private Integer index;
    @Column("SW_SYS_COMMAND_PROPERTY_EXP")
    private String propertyExpression;
    @Column("SW_SYS_COMMAND_TEMPVALUE")
    private String tempValue;
}
