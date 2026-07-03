package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("SW_SYS_MODEL_TRIGGER")
public class Trigger {
    @Id
    @Column("SysId")
    private Long id;
    @Column("SW_MODEL_TRIGGER_ARGMODEL")
    private Long argModelId;
    @Column("SW_MODEL_TRIGGER_TYPE")
    private ModelTriggerType triggerType;
    @Column("SW_MODEL_TRIGGER_FILTER")
    private String filter;
    @Column("SW_MODEL_TRIGGER_ARGFILTER")
    private String argFilter;
    @Column("SW_MODEL_TRIGGER_OPERATIONTYPE")
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
