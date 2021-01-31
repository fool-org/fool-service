package org.fool.framework.view.service;

import org.fool.framework.dao.DaoService;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ViewDataService {

    @Autowired
    private DaoService daoService;


    public View getViewData(String viewName, String token) {
        return daoService.getOneDetailByKey(View.class, viewName);
    }
}
