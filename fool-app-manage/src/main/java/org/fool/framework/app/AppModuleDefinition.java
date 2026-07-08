package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.model.model.Model;
import org.fool.framework.view.model.View;

import java.util.List;

@Data
public class AppModuleDefinition {
    private String name;
    private String remark;
    private String assembly;
    private String fileName;
    private String version;
    private Boolean generationCode = true;
    private List<AppModuleDefinition> dependencies = List.of();
    private List<Model> models = List.of();
    private List<View> views = List.of();

    public static AppModuleDefinition legacy(
            String name,
            String remark,
            String version,
            List<Model> models) {
        AppModuleDefinition module = new AppModuleDefinition();
        module.setName(name);
        module.setRemark(remark);
        module.setAssembly(name);
        module.setFileName(name + ".dll");
        module.setVersion(version);
        module.setGenerationCode(true);
        module.setModels(models == null ? List.of() : models);
        return module;
    }
}
