package org.fool.framework.view.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;

@Data
public class PersistedViewOperation {
    @Column("SysId")
    private Long id;
    @Column("SW_SYS_VIEW_OperationsVIEW_ID")
    private Long ownerViewId;
    @Column("SW_VIEW_OPERATION_NAME")
    private String name;
    @Column("SW_VIEW_OPERATION_MODELOPERATION")
    private Long operationId;
    @Column("SW_VIEW_OPERATION_RESULTVIEW")
    private Long resultViewId;
    @Column("SW_VIEW_OPERATION_INDEX")
    private Integer location;
    @Column("SW_VIEW_OPERATION_REQUIRESELECTB")
    private Boolean requireSelect;
    @Column("SW_MODEL_OPERATION_FILTER")
    private String filter;
    @Column("SW_MODEL_OPERATION_BASETYPE")
    private OperationBaseType operationBaseType;
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
    @Column("operation_view_id")
    private Long operationViewId;
    @Column("SW_SYS_OPVIEW_SUCCESMSG")
    private String successMsg;
    @Column("SW_SYS_OPVIEW_ERRORMSG")
    private String errorMsg;
    @Column("SW_SYS_OPVIEW_ConfirmMSG")
    private String confirmMsg;

    public ViewOperation toViewOperation() {
        ViewOperation viewOperation = new ViewOperation();
        viewOperation.setName(name);
        viewOperation.setLocation(location == null ? 0 : location);
        viewOperation.setRequireSelect(Boolean.TRUE.equals(requireSelect));
        viewOperation.setType(operationId == null ? ViewOperationType.MODAL_DETAIL_VIEW : ViewOperationType.COMMAND);
        viewOperation.setSuccessMsg(successMsg);
        viewOperation.setErrorMsg(errorMsg);
        viewOperation.setConfirmMsg(confirmMsg);
        if (operationId != null) {
            Operation operation = new Operation();
            operation.setId(operationId);
            operation.setName(name);
            operation.setFilter(filter);
            operation.setBaseOperationType(operationBaseType);
            operation.setArgModelId(argModelId);
            operation.setArgFilter(argFilter);
            operation.setInvokeDll(invokeDll);
            operation.setInvokeClass(invokeClass);
            operation.setInvokeMethod(invokeMethod);
            operation.setReturnModelId(returnModelId);
            viewOperation.setOperation(operation);
        }
        if (resultViewId != null) {
            View resultView = new View();
            resultView.setId(resultViewId);
            viewOperation.setResultView(resultView);
        }
        return viewOperation;
    }
}
