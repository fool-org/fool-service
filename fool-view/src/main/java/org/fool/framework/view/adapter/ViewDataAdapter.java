package org.fool.framework.view.adapter;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.model.View;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ViewDataAdapter {

    public ListViewResult getListViewResult(View view, PageResult<IDynamicData> item) {
        ListViewResult result = new ListViewResult();
        result.setPageInfo(item.getPageInfo());
        result.setItems(new LinkedList<>());
        if (!CollectionUtils.isEmpty(item.getItems())) {
            item.getItems().forEach(p -> {
                ListDataItem dataItem = new ListDataItem();
                var maps = p.toMap();

                dataItem.setValues(new LinkedHashMap<>());
                for (var viewItem : view.getListItems()) {
                    dataItem.getValues().put(viewItem.getModelProperty(), getFormat(viewItem.getFormatRegx(), p.get(viewItem.getModelProperty())));
                }
                dataItem.setId(p.getId());
                result.getItems().add(dataItem);

            });
        }

        return result;

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
