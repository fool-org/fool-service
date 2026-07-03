package org.fool.framework.view.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;

@Data
public class OperationViewParam {
    @Column("SysId")
    private Long id;
    @Column("SW_SYS_OPERATIONVIEW_ParamsSysId")
    private Long ownerOperationViewId;
    @Column("SW_SYS_OPVIEWITEM_NAME")
    private String name;
    @Column("SW_SYS_OPVIEWITEM_INDEX")
    private Integer index;
    @Column("SW_SYS_OPVIEWITEM_PARAM")
    private Long paramId;
    @Column("SW_SYS_OPERATION_PARAM_NAME")
    private String paramName;
    @Column("SW_SYS_OPERATION_PARAM_VIEW")
    private Long viewId;
    @Column("SW_SYS_OPERATION_PARAM_FILTER")
    private String filter;
    @Column("SW_SYS_OPERATION_PARAM_VALUE")
    private String value;
}
