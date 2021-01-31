package org.fool.framework.view.service;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.dto.CommonException;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.dto.PageInfo;
import org.fool.framework.view.dto.QueryValue;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class DataQueryService {
    @Autowired
    private DaoService daoService;

    @Autowired
    private ModelDataService modelDataService;

    /**
     * 得到视图信息
     *
     * @param viewName
     * @param filter
     * @param pageInfo
     */
    public void queryViewDataList(String viewName, Map<String, QueryValue> filter, PageInfo pageInfo) {

        Class modelClass = null;
        View view = daoService.getOneDetailByKey(View.class, viewName);
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());

        var properties = getViewProperies(view, model);
        IQueryFilter queryFilter = null;
        var result = modelDataService.getDataList(view.getViewModel(), queryFilter, properties);

//        var datas = ViewAdapter.getDataItem(result);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        try {
            modelClass = Class.forName(view.getViewModelClass());
        } catch (ClassNotFoundException e) {
            throw new CommonException(ErrorCode.VIEW_MODEL_NOT_FOUND, "没有查到视图对应的模型类型");
        }


    }

    private List<Property> getViewProperies(View view, Model model) {
        List<Property> result = new LinkedList<>();
        for (var item : view.getListItems()) {
            var propertyOptional = model.getProperties().stream().filter(p -> p.getName().equals(item.getModelProperty())).findFirst();
            if (propertyOptional.isPresent()) {
//                result.add(propertyOptional.get());
            }
        }
        return result;
    }

    private QueryAndArgs generateQuery(View view, Map<String, QueryValue> filter, PageInfo pageInfo) {
        return null;
    }
}
