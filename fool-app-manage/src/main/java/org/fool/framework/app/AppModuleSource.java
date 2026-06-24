package org.fool.framework.app;

import org.fool.framework.model.model.Model;

import java.util.ArrayList;
import java.util.List;

public interface AppModuleSource {
    List<AppModuleDefinition> getModules();

    List<Model> getModels(AppModuleDefinition module);

    default List<Model> getModels() {
        List<Model> models = new ArrayList<>();
        for (AppModuleDefinition module : getModules()) {
            models.addAll(getModels(module));
        }
        return models;
    }

    static AppModuleSource empty() {
        return new StaticAppModuleSource(List.of());
    }
}
