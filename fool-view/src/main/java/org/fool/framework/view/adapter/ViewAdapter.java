package org.fool.framework.view.adapter;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.OperationInfo;
import org.fool.framework.view.dto.TableColumnInfo;
import org.fool.framework.view.dto.ViewInputInfo;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.springframework.stereotype.Component;

import java.util.Comparator;
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
        result.setAutoFreshTime(safeAutoFreshTime(view));
        result.setTableColumn(new LinkedList<>());
        orderedListItems(view).stream().filter(p -> p.getEditType() != ItemEditType.Format).forEach(p -> {
            result.getTableColumn().add(
                    TableColumnInfo.builder().property(p.getModelProperty())
                            .title(p.getItemLabel())
                            .showIndex(safeShowIndex(p))
                            .width(safeWidth(p))
                            .build());
        });
        result.setInputInfo(new LinkedList<>());
        orderedListItems(view).stream()
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

    private List<ViewItem> orderedListItems(View view) {
        return view.getListItems().stream()
                .sorted(Comparator.comparingInt(this::safeShowIndex))
                .toList();
    }

    private int safeShowIndex(ViewItem item) {
        return item.getShowIndex() == null ? 0 : item.getShowIndex();
    }

    private int safeWidth(ViewItem item) {
        return item.getWidth() == null ? 0 : item.getWidth();
    }

    private Integer safeAutoFreshTime(View view) {
        return view.getAutoFreshInterval() == null ? 0 : view.getAutoFreshInterval();
    }

    private OperationInfo toOperationInfo(ViewOperation operation) {
        OperationInfo result = new OperationInfo();
        result.setId(operationId(operation));
        result.setText(operation.getName());
        result.setRequireSelect(operation.isRequireSelect());
        result.setType(operation.getType());
        result.setLocation(operation.getLocation());
        if (operation.getResultView() == null) {
            result.setViewName("");
            result.setViewId(0L);
        } else {
            result.setViewName(operation.getResultView().getViewName());
            result.setViewId(operation.getResultView().getId());
        }
        return result;
    }

    private Long operationId(ViewOperation operation) {
        if (operation.getOperation() == null || operation.getOperation().getId() == null) {
            return 0L;
        }
        return operation.getOperation().getId();
    }

}
