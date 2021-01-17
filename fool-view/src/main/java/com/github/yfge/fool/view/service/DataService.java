package com.github.yfge.fool.view.service;

import com.github.yfge.fool.dao.DaoService;
import com.github.yfge.fool.dto.CommonException;
import com.github.yfge.fool.view.common.ErrorCode;
import com.github.yfge.fool.view.dto.PageInfo;
import com.github.yfge.fool.view.dto.QueryValue;
import com.github.yfge.fool.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DataService {
    @Autowired
    private DaoService daoService;

    public void queryViewDataList(String viewName, Map<String, QueryValue> filter, PageInfo pageInfo) {
        Class modelClass = null;
        View view = daoService.getOneByKey(View.class, viewName);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        try {
            modelClass = Class.forName(view.getModelClass());
        } catch (ClassNotFoundException e) {
            throw new CommonException(ErrorCode.VIEW_MODEL_NOT_FOUND, "没有查到视图对应的模型类型");
        }


    }
}
