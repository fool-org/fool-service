package org.fool.framework.app.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "fool.app.initialization")
public class AppInitializationProperties {
    private boolean enabled;
    private String moduleName = "FoolSystem";
    private String moduleRemark = "fool-service framework system models";
    private String moduleVersion = "1.0.0";
    private String rootPackage = "org.fool.framework.model.model";
    private List<String> dependencyPackages = new ArrayList<>(List.of(
            "org.fool.framework.view.model",
            "org.fool.framework.app",
            "org.fool.framework.auth.foolframework.auth",
            "org.fool.framework.dbmanage",
            "org.fool.framework.event"));
    private String metadataConnection;
    private String dataConnection;
    private boolean defaultApplicationEnabled;
    private String defaultApplicationId = "fool-service";
    private String defaultApplicationKey = "fool-service";
    private String defaultApplicationName = "Fool System";
    private String defaultApplicationVersion = "1.0.0";
    private String defaultAdministratorId = "admin";
    private String defaultDatabaseId = "car_wash";
    private String defaultDatabaseName = "car_wash";
    private Long defaultViewId = 100L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleRemark() {
        return moduleRemark;
    }

    public void setModuleRemark(String moduleRemark) {
        this.moduleRemark = moduleRemark;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getRootPackage() {
        return rootPackage;
    }

    public void setRootPackage(String rootPackage) {
        this.rootPackage = rootPackage;
    }

    public List<String> getDependencyPackages() {
        return dependencyPackages;
    }

    public void setDependencyPackages(List<String> dependencyPackages) {
        this.dependencyPackages = dependencyPackages == null
                ? new ArrayList<>()
                : new ArrayList<>(dependencyPackages);
    }

    public String getMetadataConnection() {
        return metadataConnection;
    }

    public void setMetadataConnection(String metadataConnection) {
        this.metadataConnection = metadataConnection;
    }

    public String getDataConnection() {
        return dataConnection;
    }

    public void setDataConnection(String dataConnection) {
        this.dataConnection = dataConnection;
    }

    public boolean isDefaultApplicationEnabled() {
        return defaultApplicationEnabled;
    }

    public void setDefaultApplicationEnabled(boolean defaultApplicationEnabled) {
        this.defaultApplicationEnabled = defaultApplicationEnabled;
    }

    public String getDefaultApplicationId() {
        return defaultApplicationId;
    }

    public void setDefaultApplicationId(String defaultApplicationId) {
        this.defaultApplicationId = defaultApplicationId;
    }

    public String getDefaultApplicationKey() {
        return defaultApplicationKey;
    }

    public void setDefaultApplicationKey(String defaultApplicationKey) {
        this.defaultApplicationKey = defaultApplicationKey;
    }

    public String getDefaultApplicationName() {
        return defaultApplicationName;
    }

    public void setDefaultApplicationName(String defaultApplicationName) {
        this.defaultApplicationName = defaultApplicationName;
    }

    public String getDefaultApplicationVersion() {
        return defaultApplicationVersion;
    }

    public void setDefaultApplicationVersion(String defaultApplicationVersion) {
        this.defaultApplicationVersion = defaultApplicationVersion;
    }

    public String getDefaultAdministratorId() {
        return defaultAdministratorId;
    }

    public void setDefaultAdministratorId(String defaultAdministratorId) {
        this.defaultAdministratorId = defaultAdministratorId;
    }

    public String getDefaultDatabaseId() {
        return defaultDatabaseId;
    }

    public void setDefaultDatabaseId(String defaultDatabaseId) {
        this.defaultDatabaseId = defaultDatabaseId;
    }

    public String getDefaultDatabaseName() {
        return defaultDatabaseName;
    }

    public void setDefaultDatabaseName(String defaultDatabaseName) {
        this.defaultDatabaseName = defaultDatabaseName;
    }

    public Long getDefaultViewId() {
        return defaultViewId;
    }

    public void setDefaultViewId(Long defaultViewId) {
        this.defaultViewId = defaultViewId;
    }
}
