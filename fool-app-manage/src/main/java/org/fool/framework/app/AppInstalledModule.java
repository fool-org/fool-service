package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_MODULE")
@Data
public class AppInstalledModule {
    @Id
    @Column("MODULE_NAME")
    private String moduleName;
    @Column("MODULE_REMARK")
    private String remark;
    @Column("MODULE_ASSEMBLY")
    private String assembly;
    @Column("MODULE_FILENAME")
    private String fileName;
    @Column("MODULE_VERSION")
    private String version;
    @Column("MODULE_GENERATIONCODE")
    private Boolean generationCode;
    @Column("MODULE_CON")
    private String connection;

    public static AppInstalledModule legacyRootModule(
            String moduleName,
            String sourceType,
            String version,
            String connection) {
        AppInstalledModule module = new AppInstalledModule();
        module.setModuleName(moduleName);
        module.setRemark(sourceType);
        module.setAssembly(moduleName);
        module.setFileName(moduleName + ".dll");
        module.setVersion(version);
        module.setGenerationCode(true);
        module.setConnection(connection);
        return module;
    }

    public static AppInstalledModule fromDefinition(AppModuleDefinition definition, String connection) {
        AppInstalledModule module = new AppInstalledModule();
        module.setModuleName(definition.getName());
        module.setRemark(definition.getRemark());
        module.setAssembly(definition.getAssembly());
        module.setFileName(definition.getFileName());
        module.setVersion(definition.getVersion());
        module.setGenerationCode(definition.getGenerationCode());
        module.setConnection(connection);
        return module;
    }
}
