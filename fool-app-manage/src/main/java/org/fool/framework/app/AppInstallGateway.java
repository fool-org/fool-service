package org.fool.framework.app;

import org.fool.framework.model.model.Model;

import java.util.List;

public interface AppInstallGateway {
    ApplicationDefinition createApplication(ApplicationDefinition app);

    void installApplicationModules(String sysCon);

    void installAuthorizationModules(String sysCon);

    void createAuthorizedUser(String sysCon, String userId);

    void installUserModules(String sysCon, String databaseConnection);

    List<String> installModuleSource(String sysCon, String databaseConnection, AppModuleSource source);

    List<String> installModelSchemas(String sysCon, String databaseConnection, List<Model> models);

    List<String> installDefaultViews(String sysCon, String databaseConnection, List<Model> models);

    Long prepareAppSystemView(String sysCon, String viewName);

    void createMenu(String sysCon, BootstrapMenuItem menu);

    void createRole(String sysCon, BootstrapRole role);
}
