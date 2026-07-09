package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("SW_SYS_OPERATION")
public class Operation {
    @Id
    @Column("SysId")
    private Long id;
    @Column("SW_SYS_MODEL_OperationsMODEL_ID")
    private Long ownerModelId;
    @Column("SW_MODEL_OPERATION_NAME")
    private String name;
    @Column("SW_MODEL_OPERATION_FILTER")
    private String filter;
    @Column("SW_MODEL_OPERATION_BASETYPE")
    private OperationBaseType baseOperationType = OperationBaseType.NULL;
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
    @Column(noMap = true)
    private List<OperationCommand> commands = new ArrayList<>();
    @Column(noMap = true)
    private List<OperationParam> params = new ArrayList<>();
}
