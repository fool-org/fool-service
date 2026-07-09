package org.fool.framework.view.adapter;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.OperationInfo;
import org.fool.framework.view.dto.OperationParamInfo;
import org.fool.framework.view.dto.ReadItemViewDetailInfo;
import org.fool.framework.view.dto.ReadItemViewInfo;
import org.fool.framework.view.dto.ReadItemViewItemInfo;
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
import java.util.function.Function;

@Component
public class ViewAdapter {

    public static void getDataItem(List<IDynamicData> result) {

    }

    public ListViewInfo getViewInfo(View view) {
        ListViewInfo result = new ListViewInfo();

        result.setId(view.getId());
        result.setViewName(view.getViewName());
        result.setName(view.getViewName());
        result.setViewTitle(view.getViewTitle());
        result.setViewType(view.getViewType());
        result.setType(view.getViewType());
        result.setShowType(view.getViewType());
        result.setTempFile(safeTempFile(view));
        result.setBrowserTitle(view.getViewRemark());
        result.setDetailViewId(safeDetailViewId(view));
        result.setAutoFreshTime(safeAutoFreshTime(view));
        result.setTableColumn(new LinkedList<>());
        orderedListScalarItems(view).stream().filter(p -> p.getEditType() != ItemEditType.Format).forEach(p -> {
            result.getTableColumn().add(
                    TableColumnInfo.builder().id(p.getId())
                            .name(p.getItemName())
                            .property(p.getModelProperty())
                            .propertyName(safePropertyName(p))
                            .title(p.getItemLabel())
                            .showIndex(safeShowIndex(p))
                            .width(safeWidth(p))
                            .format(p.getFormatRegx())
                            .isReadOnly(!p.isCanEdit())
                            .editType(p.getEditType())
                            .propertyId(0L)
                            .listViewId(safeViewId(p.getListViewId()))
                            .listViewType(0)
                            .editViewId(safeViewId(p.getEditViewId()))
                            .editExp(0L)
                            .propertyType(safePropertyType(p))
                            .propertyModel(safePropertyModel(p))
                            .viewFile(p.getViewFile())
                            .build());
        });
        result.setInputInfo(new LinkedList<>());
        orderedListScalarItems(view).stream()
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

    public ReadItemViewInfo getReadItemView(View view) {
        return getReadItemView(view, id -> null);
    }

    public ReadItemViewInfo getReadItemView(View view, Function<Long, View> viewResolver) {
        ReadItemViewInfo result = new ReadItemViewInfo();
        result.setViewName(view.getViewName());
        result.setViewId(view.getId());
        result.setItems(new LinkedList<>());
        result.setDetailViews(new LinkedList<>());
        orderedListItems(view).forEach(item -> {
            if (safeIsCollection(item)) {
                result.getDetailViews().add(toReadDetailItem(item, viewResolver));
            } else {
                result.getItems().add(toReadItem(item));
            }
        });
        return result;
    }

    private List<ViewItem> orderedListItems(View view) {
        return view.getListItems().stream()
                .sorted(Comparator.comparingInt(this::safeShowIndex))
                .toList();
    }

    private List<ViewItem> orderedListScalarItems(View view) {
        return orderedListItems(view).stream()
                .filter(item -> !safeIsCollection(item))
                .toList();
    }

    private int safeShowIndex(ViewItem item) {
        return item.getShowIndex() == null ? 0 : item.getShowIndex();
    }

    private int safeWidth(ViewItem item) {
        return item.getWidth() == null ? 0 : item.getWidth();
    }

    private long safeDetailViewId(View view) {
        return view.getDefaultDetailView() == null || view.getDefaultDetailView().getId() == null
                ? 0L
                : view.getDefaultDetailView().getId();
    }

    private Integer safeAutoFreshTime(View view) {
        return view.getAutoFreshInterval() == null ? 0 : view.getAutoFreshInterval();
    }

    private String safeTempFile(View view) {
        return view.getTempFile() == null ? "" : view.getTempFile();
    }

    private PropertyType safePropertyType(ViewItem item) {
        Property property = item.getProperty();
        return property == null || property.getPropertyType() == null
                ? PropertyType.String
                : property.getPropertyType();
    }

    private Long safePropertyModel(ViewItem item) {
        Property property = item.getProperty();
        Model model = property == null ? null : property.getPropertyModel();
        return model == null || model.getId() == null ? 0L : model.getId();
    }

    private long safeViewId(Long viewId) {
        return viewId == null ? 0L : viewId;
    }

    private String safePropertyName(ViewItem item) {
        Property property = item.getProperty();
        return property == null || property.getName() == null ? "" : property.getName();
    }

    private boolean safeIsCollection(ViewItem item) {
        Property property = item.getProperty();
        return property != null && Boolean.TRUE.equals(property.getIsCollection());
    }

    private ReadItemViewItemInfo toReadItem(ViewItem item) {
        ReadItemViewItemInfo result = new ReadItemViewItemInfo();
        fillReadItem(item, result);
        return result;
    }

    private ReadItemViewDetailInfo toReadDetailItem(ViewItem item, Function<Long, View> viewResolver) {
        ReadItemViewDetailInfo result = new ReadItemViewDetailInfo();
        fillReadItem(item, result);
        View editView = item.getEditViewId() == null ? null : viewResolver.apply(item.getEditViewId());
        result.setItems(editView == null ? List.of() : orderedListItems(editView).stream().map(this::toReadItem).toList());
        return result;
    }

    private void fillReadItem(ViewItem item, ReadItemViewItemInfo result) {
        result.setName(item.getItemName());
        result.setIndex(safeShowIndex(item));
        result.setPrpType(safePropertyType(item));
        result.setPrpId(safeReadPropertyName(item));
        result.setPrpModelId(safePropertyModel(item));
        result.setId(item.getId() == null ? "" : item.getId().toString());
        result.setPrpShowName(item.getItemLabel());
        result.setReadOnly(!item.isCanEdit());
        result.setEditType(item.getEditType());
    }

    private String safeReadPropertyName(ViewItem item) {
        String propertyName = safePropertyName(item);
        return propertyName.isEmpty() && item.getModelProperty() != null ? item.getModelProperty() : propertyName;
    }

    private OperationInfo toOperationInfo(ViewOperation operation) {
        OperationInfo result = new OperationInfo();
        result.setId(operationId(operation));
        result.setName(operation.getName());
        result.setText(operation.getName());
        result.setRequireSelect(operation.isRequireSelect());
        result.setType(operation.getType());
        result.setLocation(operation.getLocation());
        result.setParams(operation.getParams().stream().map(OperationParamInfo::from).toList());
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
