package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dto.CommonException;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.model.OperationViewParam;
import org.fool.framework.view.model.PersistedViewOperation;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;


@Slf4j
@Component
public class ViewDataService {
    private static final String VIEW_OPERATION_SQL = "SELECT "
            + "vo.`SysId`, vo.`SW_SYS_VIEW_OperationsVIEW_ID`, vo.`SW_VIEW_OPERATION_NAME`, "
            + "opv.`SW_SYS_OPVIEW_OPREATION` AS `SW_VIEW_OPERATION_MODELOPERATION`, "
            + "vo.`SW_VIEW_OPERATION_RESULTVIEW`, "
            + "vo.`SW_VIEW_OPERATION_INDEX`, vo.`SW_VIEW_OPERATION_REQUIRESELECTB`, "
            + "op.`SW_MODEL_OPERATION_FILTER`, op.`SW_MODEL_OPERATION_BASETYPE`, "
            + "op.`SW_MODEL_OPERATION_ARGMODEL`, op.`SW_MODEL_OPERATION_ARGFILTER`, "
            + "op.`SW_MODEL_OPERATION_INVOKEDLL`, op.`SW_MODEL_OPERATION_INVOKECLASS`, "
            + "op.`SW_MODEL_OPERATION_INVOKEMETHOD`, op.`SW_MODEL_OPERATION_RETURNMODEL`, "
            + "opv.`SysId` AS `operation_view_id`, "
            + "opv.`SW_SYS_OPVIEW_SUCCESMSG`, "
            + "opv.`SW_SYS_OPVIEW_ERRORMSG`, opv.`SW_SYS_OPVIEW_ConfirmMSG` "
            + "FROM `SW_SYS_VIEW_OPERATION` vo "
            + "LEFT JOIN `SW_SYS_OPERATIONVIEW` opv ON opv.`SysId` = vo.`SW_VIEW_OPERATION_MODELOPERATION` "
            + "LEFT JOIN `SW_SYS_OPERATION` op ON op.`SysId` = opv.`SW_SYS_OPVIEW_OPREATION` "
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
    private static final String OPERATION_VIEW_PARAM_SQL = "SELECT "
            + "ovi.`SysId`, ovi.`SW_SYS_OPERATIONVIEW_ParamsSysId`, "
            + "ovi.`SW_SYS_OPVIEWITEM_NAME`, ovi.`SW_SYS_OPVIEWITEM_INDEX`, "
            + "ovi.`SW_SYS_OPVIEWITEM_PARAM`, "
            + "op.`SW_SYS_OPERATION_PARAM_NAME`, op.`SW_SYS_OPERATION_PARAM_VIEW`, "
            + "op.`SW_SYS_OPERATION_PARAM_FILTER`, op.`SW_SYS_OPERATION_PARAM_VALUE` "
            + "FROM `SW_SYS_OPERATIONVIEW_ITEM` ovi "
            + "LEFT JOIN `SW_SYS_OPERATION_PARAM` op ON op.`SysId` = ovi.`SW_SYS_OPVIEWITEM_PARAM` "
            + "WHERE ovi.`SW_SYS_OPERATIONVIEW_ParamsSysId` = ? "
            + "ORDER BY ovi.`SW_SYS_OPVIEWITEM_INDEX`, ovi.`SysId`";
    private static final String DEFAULT_DETAIL_VIEW_SQL = "SELECT `VIEW_DEFAULT` "
            + "FROM `SW_SYS_VIEW` "
            + "WHERE `VIEW_ID` = ? AND `VIEW_DEFAULT` IS NOT NULL";

    @Autowired
    private DaoService daoService;

    public View getViewData(String viewId, String token) {
        var view = daoService.getOneDetailByKey(View.class, requireViewId(viewId));
        attachProperties(view);
        attachDefaultDetailView(view);
        attachOperations(view);
        return view;
    }

    static String requireViewId(String viewId) {
        String normalized = StringUtils.hasText(viewId) ? viewId.trim() : "";
        if (normalized.isEmpty() || !normalized.chars().allMatch(ch -> ch >= '0' && ch <= '9')) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "ViewId is required");
        }
        return normalized;
    }

    private void attachDefaultDetailView(View view) {
        if (view == null || view.getId() == null) {
            return;
        }
        List<DefaultDetailViewRow> rows = daoService.selectList(
                DefaultDetailViewRow.class, DEFAULT_DETAIL_VIEW_SQL, view.getId());
        if (CollectionUtils.isEmpty(rows) || rows.get(0).viewId == null) {
            return;
        }
        View detailView = new View();
        detailView.setId(rows.get(0).viewId);
        view.setDefaultDetailView(detailView);
    }

    private void attachOperations(View view) {
        if (view == null || view.getId() == null) {
            return;
        }
        var rows = daoService.selectList(PersistedViewOperation.class, VIEW_OPERATION_SQL, view.getId());
        var operations = rows.stream()
                .map(PersistedViewOperation::toViewOperation)
                .toList();
        for (int i = 0; i < rows.size(); i++) {
            Long operationViewId = rows.get(i).getOperationViewId();
            if (operationViewId != null) {
                operations.get(i).setParams(daoService.selectList(
                        OperationViewParam.class, OPERATION_VIEW_PARAM_SQL, operationViewId));
            }
        }
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

    public static final class DefaultDetailViewRow {
        @Column("VIEW_DEFAULT")
        public Long viewId;
    }
}
