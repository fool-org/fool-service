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
}
