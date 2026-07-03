package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("SW_SYS_PROPERTY_TRIGGER")
public class PropertyTrigger {
    @Id
    @Column("SysId")
    private Long id;
    @Column("SW_SYS_PROPERTY_TriggersSysId")
    private Long ownerPropertyId;
    @Column("SW_PROPERTY_TRIGGER_ARGFILTER")
    private String argFilter;
    @Column("SW_PROPERTY_TRIGGER_ARGMODEL")
    private Long argModelId;
    @Column("SW_PROPERTY_TRIGGER_FILTER")
    private String filter;
    @Column("SW_PROPERTY_TRIGGER_TYPE")
    private PropertyTriggerType triggerType;
    @Column("SW_PROPERTY_TRIGGER_NAME")
    private String name;
    @Column("SW_PROPERTY_TRIGGER_PROPERTY")
    private Long propertyId;
    @Column("SW_PROPERTY_TRIGGER_BASETYPE")
    private OperationBaseType baseOperationType = OperationBaseType.NULL;
    @Column("SW_MODEL_TRIGGER_INVOKEDLL")
    private String invokeDll;
    @Column("SW_MODEL_TRIGGER_INVOKECLASS")
    private String invokeClass;
    @Column("SW_MODEL_TRIGGER_INVOKEMETHOD")
    private String invokeMethod;
    @Column(noMap = true)
    private List<OperationCommand> commands = new ArrayList<>();
}
