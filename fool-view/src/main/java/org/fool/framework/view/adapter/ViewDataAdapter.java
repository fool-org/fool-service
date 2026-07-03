package org.fool.framework.view.adapter;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageNavigatorResult;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.OperationInfo;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ViewDataAdapter {

    public ListViewResult getListViewResult(View view, PageResult<IDynamicData> item) {
        ListViewResult result = new ListViewResult();
        result.setPageInfo(item.getPageInfo());
        PageNavigatorResult pageInfo = item.getPageInfo();
        result.setTotalItem(pageInfo == null ? 0L : pageInfo.getTotal());
        result.setTotalPage(pageInfo == null ? 0L : pageInfo.getPageCount());
        result.setPageIndex(pageInfo == null ? 0L : pageInfo.getPageIndex());
        result.setFreshTime(LocalDateTime.now());
        result.setAutoFreshTime(safeAutoFreshTime(view));
        List<ViewItem> listItems = orderedListScalarItems(view);
        result.setCols(listItems.stream()
                .filter(p -> p.getEditType() != ItemEditType.Format)
                .map(this::columnName)
                .collect(Collectors.toList()));
        result.setItems(new LinkedList<>());
        if (!CollectionUtils.isEmpty(item.getItems())) {
            for (int itemIndex = 0; itemIndex < item.getItems().size(); itemIndex++) {
                IDynamicData p = item.getItems().get(itemIndex);
                ListDataItem dataItem = new ListDataItem();

                dataItem.setValues(new LinkedHashMap<>());
                dataItem.setItems(new LinkedList<>());
                dataItem.setRowIndex(rowIndex(item.getPageInfo(), itemIndex));
                for (var viewItem : listItems) {
                    Object rawValue = p.get(viewItem.getModelProperty());
                    if (viewItem.getEditType() == ItemEditType.Format) {
                        dataItem.setRowFmt(formatRow(rawValue));
                        continue;
                    }
                    Object formattedValue = getFormat(viewItem.getFormatRegx(), rawValue);
                    dataItem.getValues().put(viewItem.getModelProperty(), formattedValue);
                    dataItem.getItems().add(legacyValueItem(viewItem, rawValue, formattedValue));
                }
                dataItem.setId(p.getId());
                result.getItems().add(dataItem);

            }
        }
        result.setData(result.getItems());

        return result;

    }

    public QueryDataDetailResult getDetailViewResult(View view, IDynamicData data) {
        QueryDataDetailResult result = new QueryDataDetailResult();
        result.setAutoFreshTime(safeAutoFreshTime(view));
        result.setCanEdit(true);
        result.setOperations(view.getOperations().stream().map(this::legacyOperation).toList());
        QueryDataDetailResult.DataDetail detail = new QueryDataDetailResult.DataDetail();
        detail.setObjId(data == null ? "" : formatRow(data.getId()));
        detail.setName(view.getViewName());
        detail.setModel(view.getViewModel());
        detail.setParentId("");
        detail.setItems(collectionItems(view, data));
        detail.setSimpleData(orderedListItems(view).stream()
                .filter(item -> item.getEditType() != ItemEditType.Format)
                .filter(item -> !safeIsCollection(item))
                .map(item -> {
                    Object rawValue = data == null ? null : data.get(item.getModelProperty());
                    Object formattedValue = getFormat(item.getFormatRegx(), rawValue);
                    return legacyValueItem(item, rawValue, formattedValue);
                })
                .toList());
        result.setData(detail);
        return result;
    }

    private OperationInfo legacyOperation(ViewOperation operation) {
        OperationInfo result = new OperationInfo();
        result.setId(operation.getOperation() == null || operation.getOperation().getId() == null
                ? 0L
                : operation.getOperation().getId());
        result.setName(operation.getName());
        result.setText(operation.getName());
        result.setType(operation.getType());
        result.setLocation(operation.getLocation());
        result.setRequireSelect(operation.isRequireSelect());
        result.setViewId(operation.getResultView() == null || operation.getResultView().getId() == null
                ? 0L
                : operation.getResultView().getId());
        result.setViewName(operation.getResultView() == null || operation.getResultView().getViewName() == null
                ? ""
                : operation.getResultView().getViewName());
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

    private long rowIndex(PageNavigatorResult pageInfo, int itemIndex) {
        if (pageInfo == null || pageInfo.getPageSize() <= 0 || pageInfo.getPageIndex() <= 0) {
            return itemIndex + 1L;
        }
        return (pageInfo.getPageIndex() - 1L) * pageInfo.getPageSize() + itemIndex + 1L;
    }

    private Integer safeAutoFreshTime(View view) {
        return view.getAutoFreshInterval() == null ? 0 : view.getAutoFreshInterval();
    }

    private boolean safeIsCollection(ViewItem item) {
        Property property = item.getProperty();
        return property != null && Boolean.TRUE.equals(property.getIsCollection());
    }

    private String columnName(ViewItem viewItem) {
        return StringUtils.isEmpty(viewItem.getItemName()) ? viewItem.getModelProperty() : viewItem.getItemName();
    }

    private String formatRow(Object value) {
        return value == null ? "" : value.toString();
    }

    private ListDataValue legacyValueItem(ViewItem viewItem, Object rawValue, Object formattedValue) {
        Property property = viewItem.getProperty();
        return legacyValueProperty(
                property,
                property == null || property.getName() == null ? viewItem.getModelProperty() : property.getName(),
                columnName(viewItem),
                !viewItem.isCanEdit(),
                viewItem.getEditType(),
                rawValue,
                formattedValue);
    }

    private ListDataValue legacyValueProperty(
            Property property,
            String propertyName,
            String showName,
            boolean readOnly,
            ItemEditType editType,
            Object rawValue,
            Object formattedValue) {
        ListDataValue result = new ListDataValue();
        PropertyType propertyType = property == null || property.getPropertyType() == null
                ? PropertyType.String
                : property.getPropertyType();
        result.setObjId(legacyObjId(propertyType, rawValue));
        result.setPrpId(propertyName);
        result.setFmtValue(legacyFmtValue(property, propertyType, rawValue, formattedValue));
        result.setPrpShowName(showName);
        result.setPrpType(propertyType);
        Model propertyModel = property == null ? null : property.getPropertyModel();
        result.setPrpModelId(propertyModel == null || propertyModel.getId() == null ? 0L : propertyModel.getId());
        result.setReadOnly(readOnly);
        result.setEditType(editType);
        return result;
    }

    private List<QueryDataDetailResult.PropertyDataItems> collectionItems(View view, IDynamicData data) {
        return orderedListItems(view).stream()
                .filter(item -> item.getEditType() != ItemEditType.Format)
                .filter(this::safeIsCollection)
                .map(item -> collectionItem(item, data))
                .toList();
    }

    private QueryDataDetailResult.PropertyDataItems collectionItem(ViewItem item, IDynamicData data) {
        Property property = item.getProperty();
        Model itemModel = property == null ? null : property.getPropertyModel();
        QueryDataDetailResult.PropertyDataItems result = new QueryDataDetailResult.PropertyDataItems();
        result.setProperties(childProperties(itemModel).stream()
                .map(child -> legacyValueProperty(
                        child,
                        child.getName(),
                        propertyShowName(child),
                        true,
                        ItemEditType.ReadOnly,
                        null,
                        null))
                .toList());
        result.setItems(collectionDataItems(itemModel, collectionValue(item, property, data)));
        result.setListViewId(0L);
        result.setDetailViewId(0L);
        result.setName(itemModel == null || itemModel.getName() == null ? columnName(item) : itemModel.getName());
        result.setPrpId(property == null || property.getName() == null ? item.getModelProperty() : property.getName());
        result.setSelectFromExists(false);
        result.setItemName(columnName(item));
        result.setSelectedView(0L);
        return result;
    }

    private Object collectionValue(ViewItem item, Property property, IDynamicData data) {
        if (data == null) {
            return null;
        }
        String propertyName = property == null || property.getName() == null ? item.getModelProperty() : property.getName();
        return data.get(propertyName);
    }

    private List<QueryDataDetailResult.DataItem> collectionDataItems(Model itemModel, Object value) {
        if (!(value instanceof Iterable<?> values)) {
            return List.of();
        }
        List<Property> childProperties = childProperties(itemModel);
        List<QueryDataDetailResult.DataItem> result = new LinkedList<>();
        for (Object childValue : values) {
            QueryDataDetailResult.DataItem item = new QueryDataDetailResult.DataItem();
            if (childValue instanceof IDynamicData childData) {
                item.setDataId(formatRow(childData.getId()));
                item.setValues(childProperties.stream()
                        .map(childProperty -> {
                            Object rawValue = childData.get(childProperty.getName());
                            Object formattedValue = getFormat(childProperty.getFormat(), rawValue);
                            return legacyValueProperty(
                                    childProperty,
                                    childProperty.getName(),
                                    propertyShowName(childProperty),
                                    true,
                                    ItemEditType.ReadOnly,
                                    rawValue,
                                    formattedValue);
                        })
                        .toList());
            } else {
                item.setDataId(formatRow(childValue));
                item.setValues(List.of());
            }
            result.add(item);
        }
        return result;
    }

    private List<Property> childProperties(Model itemModel) {
        if (itemModel == null || itemModel.getProperties() == null) {
            return List.of();
        }
        return itemModel.getProperties().stream()
                .filter(property -> !Boolean.TRUE.equals(property.getIsCollection()))
                .toList();
    }

    private String propertyShowName(Property property) {
        if (property == null) {
            return "";
        }
        return StringUtils.hasText(property.getRemark()) ? property.getRemark() : property.getName();
    }

    private String legacyObjId(PropertyType propertyType, Object rawValue) {
        if (propertyType == PropertyType.BusinessObject && rawValue instanceof IDynamicData data) {
            return formatRow(data.getId());
        }
        return formatRow(rawValue);
    }

    private String legacyFmtValue(Property property, PropertyType propertyType, Object rawValue, Object formattedValue) {
        return switch (propertyType) {
            case Date -> formatDate(rawValue);
            case Time -> formatTime(rawValue);
            case BusinessObject -> formatBusinessObject(property, rawValue);
            case Enum -> formatEnumValue(property, rawValue);
            default -> formatRow(formattedValue);
        };
    }

    private String formatDate(Object value) {
        if (value instanceof LocalDate date) {
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof Date date) {
            return new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return formatRow(value);
    }

    private String formatTime(Object value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if (value instanceof LocalTime time) {
            return time.format(formatter);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalTime().format(formatter);
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().format(formatter);
        }
        if (value instanceof Date date) {
            return new SimpleDateFormat("HH:mm:ss").format(date);
        }
        return formatRow(value);
    }

    private String formatBusinessObject(Property property, Object value) {
        if (value instanceof IDynamicData data) {
            Model model = property == null ? null : property.getPropertyModel();
            Property showProperty = model == null ? null : model.getShowProperty();
            if (showProperty != null && showProperty.getName() != null) {
                return formatRow(data.get(showProperty.getName()));
            }
            return formatRow(data.getId());
        }
        return formatRow(value);
    }

    private String formatEnumValue(Property property, Object value) {
        String rawValue = formatRow(value);
        Model model = property == null ? null : property.getPropertyModel();
        if (model == null || model.getEnumValues() == null) {
            return rawValue;
        }
        for (EnumValue enumValue : model.getEnumValues()) {
            if (enumValue != null && Objects.equals(enumValue.getValue(), rawValue)) {
                return enumValue.getName() == null ? rawValue : enumValue.getName();
            }
        }
        return rawValue;
    }

    private Object getFormat(String formatRegx, Object o) {

        if (StringUtils.isEmpty(formatRegx)) {
            return o;
        } else {
            try {

                ExpressionParser parser = new SpelExpressionParser();
//                #value.stream().map(p->p.get("carNo")).collect(Collectors.joining(","))
//                Expression expression = parser.parseExpression("('Hello' + ' World').concat(#end)");

                Expression expression = parser.parseExpression(formatRegx);
                EvaluationContext context = new StandardEvaluationContext();
                context.setVariable("value", o);
                var result = expression.getValue(context);
                if (result instanceof Collection) {
                    return ((Collection) result).stream().collect(Collectors.joining(","));
                }
                return result;
            } catch (Exception e) {
                log.info("", e);
                return o;
            }

        }
    }
}
