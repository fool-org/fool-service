package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.DaoService;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ViewDataService {

    @Autowired
    private DaoService daoService;

    public View getViewData(String viewName, String token) {
        var view = daoService.getOneDetailByKey(View.class, viewName);
        log.info("the view is :{}", view);
        return view;
    }
}
