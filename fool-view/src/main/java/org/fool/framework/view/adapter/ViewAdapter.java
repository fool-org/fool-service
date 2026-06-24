package org.fool.framework.view.adapter;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.OperationInfo;
import org.fool.framework.view.dto.TableColumnInfo;
import org.fool.framework.view.dto.ViewInputInfo;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ViewAdapter {

    public static void getDataItem(List<IDynamicData> result) {

    }

    public ListViewInfo getViewInfo(View view) {
        ListViewInfo result = new ListViewInfo();

        result.setId(view.getId());
        result.setViewName(view.getViewName());
        result.setViewTitle(view.getViewTitle());
        result.setViewType(view.getViewType());
        result.setBrowserTitle(view.getViewRemark());
        result.setTableColumn(new LinkedList<>());
        view.getListItems().stream().filter(p -> p.getEditType() != ItemEditType.Format).forEach(p -> {
            result.getTableColumn().add(
                    TableColumnInfo.builder().property(p.getModelProperty())
                            .title(p.getItemLabel())
                            .build());
        });
        result.setInputInfo(new LinkedList<>());
        view.getListItems().stream()
                .filter(p -> p.getEditType() != ItemEditType.Format)
                .filter(p -> p.getInputType() != InputType.READ_ONLY)
                .forEach(p -> {
            result.getInputInfo().add(
                    ViewInputInfo.builder()
                            .inputType(p.getInputType())
                            .legend(p.getItemLegend())
                            .option(new LinkedList<>())
                            .property(p.getModelProperty())
                            .text(p.getItemLabel())
                            .build()
            );
        });
        result.setOperations(new LinkedList<>());
        view.getOperations().forEach(p -> result.getOperations().add(toOperationInfo(p)));
        return result;
    }

    private OperationInfo toOperationInfo(ViewOperation operation) {
        OperationInfo result = new OperationInfo();
        result.setText(operation.getName());
        result.setRequireSelect(operation.isRequireSelect());
        result.setType(operation.getType());
        if (operation.getResultView() == null) {
            result.setViewName("");
            result.setViewId(0L);
        } else {
            result.setViewName(operation.getResultView().getViewName());
            result.setViewId(operation.getResultView().getId());
        }
        return result;
    }

}
