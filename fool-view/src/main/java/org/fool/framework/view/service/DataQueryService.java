package org.fool.framework.view.service;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.dto.CommonException;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.query.BetweenFilter;
import org.fool.framework.query.CompareFilter;
import org.fool.framework.query.CompareOp;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.SimpleFilter;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryValue;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class DataQueryService {
    @Autowired
    private DaoService daoService;

    @Autowired
    private ViewDataAdapter viewAdapter;
    @Autowired
    private ModelDataService modelDataService;

    /**
     * 得到视图信息
     *
     * @param viewName
     * @param filter
     * @param pageInfo
     */
    public ListViewResult queryViewDataList(String viewName, Map<String, QueryValue> filter, PageNavigator pageInfo) {

        View view = daoService.getOneDetailByKey(View.class, viewName);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        var properties = getViewProperies(view, model);
        IQueryFilter queryFilter = generateFilter(model, filter, view.getFilter());
        Property orderProperty = getDefaultOrderProperty(view, model);
        var result = modelDataService.getDataListWithPageInfo(
                view.getViewModel(),
                queryFilter,
                properties,
                pageInfo,
                orderProperty == null ? null : orderProperty.getColumn(),
                orderProperty != null);

        return viewAdapter.getListViewResult(view, result);
    }

    /**
     * 生成简单查询表达式
     *
     * @param model
     * @param filter
     * @return
     */
    private IQueryFilter generateFilter(Model model, Map<String, QueryValue> filter, String viewFilter) {
        IQueryFilter queryFilter = rawViewFilter(viewFilter);
        var properties = model.getProperties();
        if (filter != null) {
            for (var key : filter.keySet()
            ) {
                var value = filter.get(key);
                if (properties.stream().filter(p -> p.getName().equals(key)).count() > 0) {
                    if (!StringUtils.isEmpty(value.getValue())) {
                        /**
                         * 如果传了一个值，就是相等
                         */
                        queryFilter = queryFilter.and(new CompareFilter(properties.stream().filter(p -> p.getName().equals(key)).findFirst().get().getColumn(), CompareOp.EQUAL, filter.get(key).getValue()));
                    } else if ((!CollectionUtils.isEmpty(value.getValues())) && value.getValues().size() == 2) {
                        /**
                         * 如果传了两值就是between
                         */
                        queryFilter = queryFilter.and(new BetweenFilter(properties.stream().filter(p -> p.getName().equals(key)).findFirst().get().getColumn(), value.getValues().get(0), value.getValues().get(1)));
                    }
                }
            }
        }
        return queryFilter;
    }

    private IQueryFilter rawViewFilter(String viewFilter) {
        if (!StringUtils.hasText(viewFilter)) {
            return IQueryFilter.init();
        }
        return new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs queryAndArgs = new QueryAndArgs();
                queryAndArgs.setSql(viewFilter);
                queryAndArgs.setArgs(new Object[]{});
                return queryAndArgs;
            }
        };
    }

    private List<Property> getViewProperies(View view, Model model) {
        List<Property> result = new LinkedList<>();
        for (var item : orderedListItems(view)) {
            var propertyOptional = model.getProperties().stream().filter(p -> p.getName().equals(item.getModelProperty())).findFirst();
            if (propertyOptional.isPresent()) {
                result.add(propertyOptional.get());
            }
        }
        return result;
    }

    private Property getDefaultOrderProperty(View view, Model model) {
        for (var item : orderedListItems(view)) {
            var propertyOptional = model.getProperties().stream()
                    .filter(p -> p.getName().equals(item.getModelProperty()))
                    .findFirst();
            if (propertyOptional.isPresent()) {
                return propertyOptional.get();
            }
        }
        return null;
    }

    private List<ViewItem> orderedListItems(View view) {
        if (CollectionUtils.isEmpty(view.getListItems())) {
            return List.of();
        }
        return view.getListItems().stream()
                .sorted(Comparator.comparingInt(this::safeShowIndex))
                .toList();
    }

    private int safeShowIndex(ViewItem item) {
        return item.getShowIndex() == null ? 0 : item.getShowIndex();
    }

}
