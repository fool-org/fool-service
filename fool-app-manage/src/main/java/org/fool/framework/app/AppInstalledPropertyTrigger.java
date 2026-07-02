package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_PROPERTY_TRIGGER")
@Data
public class AppInstalledPropertyTrigger {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long triggerId;
    @Column("SW_SYS_PROPERTY_TriggersSysId")
    private Long ownerPropertyId;
    @Column("SW_PROPERTY_TRIGGER_ARGFILTER")
    private String argFilter;
    @Column("SW_PROPERTY_TRIGGER_ARGMODEL")
    private Long argModelId;
    @Column("SW_PROPERTY_TRIGGER_FILTER")
    private String filter;
    @Column("SW_PROPERTY_TRIGGER_TYPE")
    private Integer triggerType;
    @Column("SW_PROPERTY_TRIGGER_NAME")
    private String name;
    @Column("SW_PROPERTY_TRIGGER_PROPERTY")
    private Long propertyId;
    @Column("SW_PROPERTY_TRIGGER_BASETYPE")
    private Integer baseType;
    @Column("SW_MODEL_TRIGGER_INVOKEDLL")
    private String invokeDll;
    @Column("SW_MODEL_TRIGGER_INVOKECLASS")
    private String invokeClass;
    @Column("SW_MODEL_TRIGGER_INVOKEMETHOD")
    private String invokeMethod;
}
