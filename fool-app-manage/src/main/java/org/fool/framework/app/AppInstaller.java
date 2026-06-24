package org.fool.framework.app;

import org.fool.framework.model.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class AppInstaller {
    private final AppInstallGateway gateway;
    private final AppBootstrapPlan plan;

    public AppInstaller(AppInstallGateway gateway, AppBootstrapPlan plan) {
        this.gateway = gateway;
        this.plan = plan == null ? AppBootstrapPlan.legacyDefaults() : plan;
    }

    public ApplicationDefinition createApp(ApplicationDefinition app) {
        ApplicationDefinition created = gateway.createApplication(app);
        String sysCon = app.getSysCon();
        gateway.installApplicationModules(sysCon);
        gateway.installAuthorizationModules(sysCon);
        gateway.createAuthorizedUser(sysCon, app.getCreatorId());

        for (StoreDatabase database : databases(app)) {
            gateway.installUserModules(sysCon, database.getConnection());
            List<Model> schemas = modelSchemas();
            if (!schemas.isEmpty()) {
                gateway.installModuleSource(sysCon, database.getConnection(), moduleSource());
                gateway.installModelSchemas(sysCon, database.getConnection(), schemas);
                gateway.installDefaultViews(sysCon, database.getConnection(), schemas);
            }
            BootstrapMenuItem systemMenu = prepareMenu(sysCon, plan.getSystemMenu());
            BootstrapMenuItem authMenu = prepareMenu(sysCon, plan.getAuthMenu());
            gateway.createMenu(sysCon, systemMenu);
            gateway.createMenu(sysCon, authMenu);
            gateway.createRole(sysCon, adminRole(systemMenu, authMenu, app.getCreatorId()));
        }

        return created == null ? app : created;
    }

    private BootstrapMenuItem prepareMenu(String sysCon, BootstrapMenuItem template) {
        BootstrapMenuItem item = new BootstrapMenuItem(template.getText(), template.getViewName());
        if (template.getViewName() != null) {
            item.setViewId(gateway.prepareAppSystemView(sysCon, template.getViewName()));
        }
        for (BootstrapMenuItem subItem : template.getSubItems()) {
            item.getSubItems().add(prepareMenu(sysCon, subItem));
        }
        return item;
    }

    private BootstrapRole adminRole(
            BootstrapMenuItem systemMenu,
            BootstrapMenuItem authMenu,
            String authorizedUserId) {
        BootstrapRole role = new BootstrapRole(plan.getAdminRoleName(), authorizedUserId);
        addMenuWithChildren(role, systemMenu);
        addMenuWithChildren(role, authMenu);
        return role;
    }

    private static void addMenuWithChildren(BootstrapRole role, BootstrapMenuItem menu) {
        role.getItems().add(menu);
        role.getItems().addAll(menu.getSubItems());
    }

    private static List<StoreDatabase> databases(ApplicationDefinition app) {
        return app.getDataBase() == null ? List.of() : app.getDataBase();
    }

    private List<Model> modelSchemas() {
        List<Model> schemas = new ArrayList<>();
        Set<Model> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        addModels(schemas, seen, moduleSourceModels());
        addModels(schemas, seen, plan.getModelSchemas());
        return schemas;
    }

    private List<Model> moduleSourceModels() {
        return moduleSource().getModels();
    }

    private AppModuleSource moduleSource() {
        AppModuleSource source = plan.getModelModuleSource();
        return source == null ? AppModuleSource.empty() : source;
    }

    private void addModels(List<Model> target, Set<Model> seen, List<Model> models) {
        if (models == null) {
            return;
        }
        for (Model model : models) {
            if (model != null && seen.add(model)) {
                target.add(model);
            }
        }
    }
}
