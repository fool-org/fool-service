package org.fool.framework.app.autoconfigure;

import org.fool.framework.app.AppInstaller;
import org.fool.framework.app.ReflectiveAppModuleSource;
import org.fool.framework.app.SystemInitializationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

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
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
