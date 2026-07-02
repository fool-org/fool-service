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
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
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
        List<ViewItem> listItems = orderedListItems(view);
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

    private List<ViewItem> orderedListItems(View view) {
        return view.getListItems().stream()
                .sorted(Comparator.comparingInt(this::safeShowIndex))
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

    private String columnName(ViewItem viewItem) {
        return StringUtils.isEmpty(viewItem.getItemName()) ? viewItem.getModelProperty() : viewItem.getItemName();
    }

    private String formatRow(Object value) {
        return value == null ? "" : value.toString();
    }

    private ListDataValue legacyValueItem(ViewItem viewItem, Object rawValue, Object formattedValue) {
        ListDataValue result = new ListDataValue();
        Property property = viewItem.getProperty();
        PropertyType propertyType = property == null || property.getPropertyType() == null
                ? PropertyType.String
                : property.getPropertyType();
        result.setObjId(legacyObjId(propertyType, rawValue));
        result.setPrpId(property == null || property.getName() == null ? viewItem.getModelProperty() : property.getName());
        result.setFmtValue(legacyFmtValue(property, propertyType, rawValue, formattedValue));
        result.setPrpShowName(columnName(viewItem));
        result.setPrpType(propertyType);
        Model propertyModel = property == null ? null : property.getPropertyModel();
        result.setPrpModelId(propertyModel == null || propertyModel.getId() == null ? 0L : propertyModel.getId());
        result.setReadOnly(!viewItem.isCanEdit());
        result.setEditType(viewItem.getEditType());
        return result;
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
