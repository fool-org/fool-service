package org.fool.framework.view.service;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dto.CommonException;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.dto.PageInfo;
import org.fool.framework.view.dto.QueryValue;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DataService {
    @Autowired
    private DaoService daoService;

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
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        try {
            modelClass = Class.forName(view.getViewModelClass());
        } catch (ClassNotFoundException e) {
            throw new CommonException(ErrorCode.VIEW_MODEL_NOT_FOUND, "没有查到视图对应的模型类型");
        }


    }
}
