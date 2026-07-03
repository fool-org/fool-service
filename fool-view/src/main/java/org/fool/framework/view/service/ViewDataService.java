package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.view.model.PersistedViewOperation;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Slf4j
@Component
public class ViewDataService {
    private static final String VIEW_OPERATION_SQL = "SELECT "
            + "vo.`SysId`, vo.`SW_SYS_VIEW_OperationsVIEW_ID`, vo.`SW_VIEW_OPERATION_NAME`, "
            + "vo.`SW_VIEW_OPERATION_MODELOPERATION`, vo.`SW_VIEW_OPERATION_RESULTVIEW`, "
            + "vo.`SW_VIEW_OPERATION_INDEX`, vo.`SW_VIEW_OPERATION_REQUIRESELECTB`, "
            + "op.`SW_MODEL_OPERATION_BASETYPE`, opv.`SW_SYS_OPVIEW_SUCCESMSG`, "
            + "opv.`SW_SYS_OPVIEW_ERRORMSG`, opv.`SW_SYS_OPVIEW_ConfirmMSG` "
            + "FROM `SW_SYS_VIEW_OPERATION` vo "
            + "LEFT JOIN `SW_SYS_OPERATION` op ON op.`SysId` = vo.`SW_VIEW_OPERATION_MODELOPERATION` "
            + "LEFT JOIN `SW_SYS_OPERATIONVIEW` opv ON opv.`SW_SYS_OPVIEW_OPREATION` = vo.`SW_VIEW_OPERATION_MODELOPERATION` "
            + "WHERE vo.`SW_SYS_VIEW_OperationsVIEW_ID` = ? "
            + "ORDER BY vo.`SW_VIEW_OPERATION_INDEX`, vo.`SysId`";

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
        view.setOperations(daoService.selectList(PersistedViewOperation.class, VIEW_OPERATION_SQL, view.getId()).stream()
                .map(PersistedViewOperation::toViewOperation)
                .toList());
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
