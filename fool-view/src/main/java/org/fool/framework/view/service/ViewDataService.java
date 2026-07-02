package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Slf4j
@Component
public class ViewDataService {

    @Autowired
    private DaoService daoService;

    public View getViewData(String viewName, String token) {
        var view = daoService.getOneDetailByKey(View.class, viewName);
        attachProperties(view);
        return view;
    }

    private void attachProperties(View view) {
        if (view == null || CollectionUtils.isEmpty(view.getListItems()) || view.getViewModel() == null) {
            return;
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null || CollectionUtils.isEmpty(model.getProperties())) {
            return;
        }
        view.getListItems().forEach(item -> model.getProperties().stream()
                .filter(property -> item.getModelProperty() != null
                        && item.getModelProperty().equals(property.getName()))
                .findFirst()
                .ifPresent(item::setProperty));
    }
}
