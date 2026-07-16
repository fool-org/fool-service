package org.fool.framework.app.autoconfigure;

import org.fool.framework.app.AppInstaller;
import org.fool.framework.app.AppType;
import org.fool.framework.app.ApplicationDefinition;
import org.fool.framework.app.ReflectiveAppModuleSource;
import org.fool.framework.app.StoreDatabase;
import org.fool.framework.app.SystemInitializationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.time.LocalDateTime;
import java.util.List;

public class AppInitializationRunner implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppInitializationRunner.class);

    private final AppInstaller installer;
    private final AppInitializationProperties properties;

    public AppInitializationRunner(AppInstaller installer, AppInitializationProperties properties) {
        this.installer = installer;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                properties.getModuleName(),
                properties.getModuleRemark(),
                properties.getModuleVersion(),
                properties.getRootPackage(),
                properties.getDependencyPackages());
        SystemInitializationResult result = installer.initializeSystem(
                blankToNull(properties.getMetadataConnection()),
                blankToNull(properties.getDataConnection()),
                source);
        LOGGER.info(
                "Fool system initialization complete: models={}, metadata={}, ddl={}, views={}",
                result.discoveredModelCount(),
                result.installedMetadataItems().size(),
                result.schemaStatements().size(),
                result.defaultViews().size());
        if (properties.isDefaultApplicationEnabled()) {
            ApplicationDefinition app = defaultApplication();
            installer.createApp(app);
            LOGGER.info(
                    "Fool default application installation complete: app={}, administrator={}, database={}",
                    app.getAppId(),
                    app.getCreatorId(),
                    app.getDataBase().get(0).getStoreBaseId());
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ApplicationDefinition defaultApplication() {
        LocalDateTime now = LocalDateTime.now();
        String administratorId = required(properties.getDefaultAdministratorId(), "default-administrator-id");
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId(required(properties.getDefaultApplicationId(), "default-application-id"));
        app.setAppKey(required(properties.getDefaultApplicationKey(), "default-application-key"));
        app.setAppType(AppType.Web);
        app.setName(required(properties.getDefaultApplicationName(), "default-application-name"));
        app.setVersion(required(properties.getDefaultApplicationVersion(), "default-application-version"));
        app.setCreatorId(administratorId);
        app.setOwnerId(administratorId);
        app.setCreateTime(now);
        app.setUpdateTime(now);
        app.setSysCon(blankToNull(properties.getMetadataConnection()));
        app.setDefaultView(properties.getDefaultViewId());

        StoreDatabase database = new StoreDatabase();
        database.setStoreBaseId(required(properties.getDefaultDatabaseId(), "default-database-id"));
        database.setName(required(properties.getDefaultDatabaseName(), "default-database-name"));
        database.setConnection(blankToNull(properties.getDataConnection()));
        app.setDataBase(List.of(database));
        return app;
    }

    private String required(String value, String property) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("fool.app.initialization." + property + " is required.");
        }
        return value.trim();
    }
}
