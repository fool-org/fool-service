package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.model.model.Model;

import java.util.List;

@Data
public class AppBootstrapPlan {
    private final BootstrapMenuItem systemMenu;
    private final BootstrapMenuItem authMenu;
    private final String adminRoleName;
    private List<Model> modelSchemas = List.of();
    private AppModuleSource modelModuleSource = AppModuleSource.empty();

    public static AppBootstrapPlan legacyDefaults() {
        BootstrapMenuItem systemMenu = new BootstrapMenuItem("系统管理")
                .addSubItem("业务包管理", "Module列表")
                .addSubItem("模型管理", "Model列表")
                .addSubItem("连接管理", "SqlCon列表")
                .addSubItem("界面管理", "View列表")
                .addSubItem("菜单项管理", "MenuItem列表");

        BootstrapMenuItem authMenu = new BootstrapMenuItem("人员及权限")
                .addSubItem("授权用户管理", "AuthorizedUser列表")
                .addSubItem("部门管理", "Department列表")
                .addSubItem("角色管理", "Role列表");

        return new AppBootstrapPlan(systemMenu, authMenu, "应用管理员");
    }
}
