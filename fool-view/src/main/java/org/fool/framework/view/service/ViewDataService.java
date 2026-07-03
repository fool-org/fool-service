package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.view.model.PersistedViewOperation;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Slf4j
@Component
public class ViewDataService {
    private static final String VIEW_OPERATION_SQL = "SELECT "
            + "vo.`SysId`, vo.`SW_SYS_VIEW_OperationsVIEW_ID`, vo.`SW_VIEW_OPERATION_NAME`, "
            + "vo.`SW_VIEW_OPERATION_MODELOPERATION`, vo.`SW_VIEW_OPERATION_RESULTVIEW`, "
            + "vo.`SW_VIEW_OPERATION_INDEX`, vo.`SW_VIEW_OPERATION_REQUIRESELECTB`, "
            + "op.`SW_MODEL_OPERATION_FILTER`, op.`SW_MODEL_OPERATION_BASETYPE`, "
            + "op.`SW_MODEL_OPERATION_ARGMODEL`, op.`SW_MODEL_OPERATION_ARGFILTER`, "
            + "op.`SW_MODEL_OPERATION_INVOKEDLL`, op.`SW_MODEL_OPERATION_INVOKECLASS`, "
            + "op.`SW_MODEL_OPERATION_INVOKEMETHOD`, op.`SW_MODEL_OPERATION_RETURNMODEL`, "
            + "opv.`SW_SYS_OPVIEW_SUCCESMSG`, "
            + "opv.`SW_SYS_OPVIEW_ERRORMSG`, opv.`SW_SYS_OPVIEW_ConfirmMSG` "
            + "FROM `SW_SYS_VIEW_OPERATION` vo "
            + "LEFT JOIN `SW_SYS_OPERATION` op ON op.`SysId` = vo.`SW_VIEW_OPERATION_MODELOPERATION` "
            + "LEFT JOIN `SW_SYS_OPERATIONVIEW` opv ON opv.`SW_SYS_OPVIEW_OPREATION` = vo.`SW_VIEW_OPERATION_MODELOPERATION` "
            + "WHERE vo.`SW_SYS_VIEW_OperationsVIEW_ID` = ? "
            + "ORDER BY vo.`SW_VIEW_OPERATION_INDEX`, vo.`SysId`";
    private static final String OPERATION_COMMAND_SQL = "SELECT "
            + "`SysId`, `SW_SYS_OPERATION_CommandsSysId`, `SW_SYS_COMMAND_TYPE`, "
            + "`SW_SYS_COMMAND_PROPERTY`, `SW_SYS_COMMAND_EXP`, `SW_SYS_COMMAND_ARGMODEL`, "
            + "`SW_SYS_COMMAND_ARGEXP`, `SW_SYS_COMMAND_ARGID`, `SW_SYS_COMMAND_INDEX`, "
            + "`SW_SYS_COMMAND_PROPERTY_EXP`, `SW_SYS_COMMAND_TEMPVALUE` "
            + "FROM `SW_SYS_COMMANDS` "
            + "WHERE `SW_SYS_OPERATION_CommandsSysId` = ? "
            + "ORDER BY `SW_SYS_COMMAND_INDEX`, `SysId`";

    @Autowired
    private DaoService daoService;

    public View getViewData(String viewName, String token) {
        var view = daoService.getOneDetailByKey(View.class, viewName);
        attachProperties(view);
        attachOperations(view);
        return view;
    }

    private void attachOperations(View view) {
        if (view == null || view.getId() == null) {
            return;
        }
        var operations = daoService.selectList(PersistedViewOperation.class, VIEW_OPERATION_SQL, view.getId()).stream()
                .map(PersistedViewOperation::toViewOperation)
                .toList();
        operations.stream()
                .filter(operation -> operation.getOperation() != null && operation.getOperation().getId() != null)
                .forEach(operation -> {
                    List<OperationCommand> commands = daoService.selectList(
                            OperationCommand.class, OPERATION_COMMAND_SQL, operation.getOperation().getId());
                    if (!CollectionUtils.isEmpty(commands)) {
                        operation.getOperation().setCommands(commands);
                    }
                });
        view.setOperations(operations);
    }

    private void attachProperties(View view) {
        if (view == null || CollectionUtils.isEmpty(view.getListItems()) || view.getViewModel() == null) {
            return;
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null || CollectionUtils.isEmpty(model.getProperties())) {
            return;
        }
        view.getListItems().forEach(item -> model.getProperties().stream()
                .filter(property -> item.getModelProperty() != null
                        && item.getModelProperty().equals(property.getName()))
                .findFirst()
                .ifPresent(item::setProperty));
    }
}
