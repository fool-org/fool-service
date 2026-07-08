package org.fool.framework.app;

import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class StaticAppModuleSource implements AppModuleSource {
    private final List<AppModuleDefinition> modules;

    public StaticAppModuleSource(List<AppModuleDefinition> modules) {
        this.modules = modules == null ? List.of() : List.copyOf(modules);
    }

    @Override
    public List<AppModuleDefinition> getModules() {
        List<AppModuleDefinition> ordered = new ArrayList<>();
        Set<AppModuleDefinition> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (AppModuleDefinition module : modules) {
            addWithDependencies(module, visited, ordered);
        }
        return ordered;
    }

    @Override
    public List<Model> getModels(AppModuleDefinition module) {
        if (module == null || module.getModels() == null) {
            return List.of();
        }
        List<Model> ordered = new ArrayList<>();
        Set<Model> moduleModels = Collections.newSetFromMap(new IdentityHashMap<>());
        moduleModels.addAll(module.getModels());
        Set<Model> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Model> visiting = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Model model : module.getModels()) {
            addModelWithDependencies(model, moduleModels, visited, visiting, ordered);
        }
        return ordered;
    }

    @Override
    public List<View> getViews(AppModuleDefinition module) {
        return module == null || module.getViews() == null ? List.of() : module.getViews();
    }

    private void addWithDependencies(
            AppModuleDefinition module,
            Set<AppModuleDefinition> visited,
            List<AppModuleDefinition> ordered) {
        if (module == null || !visited.add(module)) {
            return;
        }
        for (AppModuleDefinition dependency : safeDependencies(module)) {
            addWithDependencies(dependency, visited, ordered);
        }
        ordered.add(module);
    }

    private List<AppModuleDefinition> safeDependencies(AppModuleDefinition module) {
        return module.getDependencies() == null ? List.of() : module.getDependencies();
    }

    private void addModelWithDependencies(
            Model model,
            Set<Model> moduleModels,
            Set<Model> visited,
            Set<Model> visiting,
            List<Model> ordered) {
        if (model == null || !moduleModels.contains(model) || visited.contains(model)) {
            return;
        }
        if (!visiting.add(model)) {
            return;
        }
        addModelWithDependencies(model.getBaseModel(), moduleModels, visited, visiting, ordered);
        for (Property property : safeProperties(model)) {
            if (Boolean.TRUE.equals(property.getIsCollection())) {
                addModelWithDependencies(property.getPropertyModel(), moduleModels, visited, visiting, ordered);
            }
        }
        visiting.remove(model);
        if (visited.add(model)) {
            ordered.add(model);
        }
    }

    private List<Property> safeProperties(Model model) {
        return model.getProperties() == null ? List.of() : model.getProperties();
    }
}
