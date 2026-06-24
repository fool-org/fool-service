package org.fool.framework.app;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AppFacade {
    private final AppRepository appRepository;

    public AppFacade(AppRepository appRepository) {
        this.appRepository = appRepository;
    }

    public ApplicationDefinition getApp(String appId, String appKey) {
        ApplicationDefinition app = appRepository.findById(appId);
        if (app != null && Objects.equals(app.getAppKey(), appKey)) {
            return app;
        }
        return null;
    }

    public ApplicationDefinition getApp(String appId) {
        return appRepository.findById(appId);
    }

    public List<ApplicationDefinition> getApps() {
        return appRepository.findAll();
    }
}
