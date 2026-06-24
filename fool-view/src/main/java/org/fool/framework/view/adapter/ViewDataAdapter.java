package org.fool.framework.view.adapter;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageNavigatorResult;
import org.fool.framework.dao.PageResult;
import org.fool.framework.view.dto.ListDataItem;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ViewDataAdapter {

    public ListViewResult getListViewResult(View view, PageResult<IDynamicData> item) {
        ListViewResult result = new ListViewResult();
        result.setPageInfo(item.getPageInfo());
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
                dataItem.setRowIndex(rowIndex(item.getPageInfo(), itemIndex));
                for (var viewItem : listItems) {
                    if (viewItem.getEditType() == ItemEditType.Format) {
                        dataItem.setRowFmt(formatRow(p.get(viewItem.getModelProperty())));
                        continue;
                    }
                    dataItem.getValues().put(viewItem.getModelProperty(), getFormat(viewItem.getFormatRegx(), p.get(viewItem.getModelProperty())));
                }
                dataItem.setId(p.getId());
                result.getItems().add(dataItem);

            }
        }

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
