package org.fool.framework.app;

import org.fool.framework.dao.DaoService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DaoAppRepository implements AppRepository {
    private final DaoService daoService;

    public DaoAppRepository(DaoService daoService) {
        this.daoService = daoService;
    }

    @Override
    public ApplicationDefinition findById(String appId) {
        return daoService.getOneByKey(ApplicationDefinition.class, appId);
    }

    @Override
    public List<ApplicationDefinition> findAll() {
        return daoService.getAllList(ApplicationDefinition.class);
    }
}
