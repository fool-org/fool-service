package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Data
@Table("SW_SYS_OPERATION_PARAM")
public class OperationParam {
    @Id
    @Column("SysId")
    private Long id;
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
