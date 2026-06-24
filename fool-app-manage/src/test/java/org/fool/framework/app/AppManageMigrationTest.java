package org.fool.framework.app;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.EncryptType;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.MultiType;
import org.fool.framework.common.annotation.ReferToProperty;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.ObjectWithSubItem;
import org.fool.framework.app.reference.shared.ReferenceCustomer;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AppManageMigrationTest {
    @Test
    public void mapsApplicationAndStoreDatabaseToLegacyTables() throws Exception {
        assertEquals("SW_APPLICATION", tableName(ApplicationDefinition.class));
        assertColumn(ApplicationDefinition.class, "appId", "SW_APP_APPLICATIONID", true);
        assertColumn(ApplicationDefinition.class, "appKey", "SW_APP_KEY", false);
        assertColumn(ApplicationDefinition.class, "appType", "SW_APP_TYPE", false);
        assertColumn(ApplicationDefinition.class, "sysCon", "SW_APP_CON", false);
        assertColumn(ApplicationDefinition.class, "defaultView", "SW_APP_VIEW", false);

        assertEquals("SW_STOREDB", tableName(StoreDatabase.class));
        assertColumn(StoreDatabase.class, "storeBaseId", "SW_STORE_STOREID", true);
        assertColumn(StoreDatabase.class, "name", "SW_STORE_NAME", false);
        assertColumn(StoreDatabase.class, "connection", "SW_STORE_CON", false);

        assertEquals("SW_APP_AUTH_USER", tableName(AuthorizedUser.class));
        assertColumn(AuthorizedUser.class, "authorizedId", "APP_AUTH_ID", true);
        assertColumn(AuthorizedUser.class, "userId", "APP_AUTH_USERID", false);
        assertColumn(AuthorizedUser.class, "userLoginName", "APP_AUTH_USERLOGINNAME", false);
        assertColumn(AuthorizedUser.class, "departmentId", "APP_AUTH_DEP", false);

        assertEquals("SW_APP_AUTH_MENU", tableName(AuthMenuItem.class));
        assertColumn(AuthMenuItem.class, "menuId", "AUTH_MENU_ID", true);
        assertColumn(AuthMenuItem.class, "text", "AUTH_MENU_TEXT", false);
        assertColumn(AuthMenuItem.class, "shortcutKey", "AUTH_MENU_SHORTCUTKEY", false);
        assertColumn(AuthMenuItem.class, "image", "AUTH_MENU_IMAGE", false);
        assertColumn(AuthMenuItem.class, "defaultVisible", "AUTH_MENU_VISIABLE", false);
        assertColumn(AuthMenuItem.class, "defaultEnable", "AUTH_MENU_ENABLE", false);
        assertColumn(AuthMenuItem.class, "viewId", "AUTH_MENU_VIEWID", false);
        assertColumn(AuthMenuItem.class, "templateFile", "AUTH_MENU_TEMPLATEFILE", false);
        assertColumn(AuthMenuItem.class, "index", "AUTH_MENU_INDEX", false);

        assertEquals("SW_APP_AUTH_MENU_SubItems", tableName(AuthMenuSubItemRelation.class));
        assertColumn(AuthMenuSubItemRelation.class, "parentMenuId", "SW_APP_AUTH_MENU_SubItemsAUTH_MENU_ID", false);
        assertColumn(AuthMenuSubItemRelation.class, "subItemMenuId", "SW_APP_AUTH_MENU_SUBITEMS_ITEM", false);

        assertEquals("SW_APP_AUTH_ROLE", tableName(AuthRole.class));
        assertColumn(AuthRole.class, "roleId", "AUTH_ROLE_ID", true);
        assertColumn(AuthRole.class, "roleName", "AUTH_ROLE_NAME", false);

        assertEquals("SW_APP_AUTH_ROLE_SW_APP_AUTH_USER", tableName(AuthRoleAuthorizedUserRelation.class));
        assertColumn(AuthRoleAuthorizedUserRelation.class, "roleId", "SW_APP_AUTH_ROLE_ID", false);
        assertColumn(AuthRoleAuthorizedUserRelation.class, "authorizedUserId", "SW_APP_AUTH_USER_ID", false);

        assertEquals("SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE", tableName(AuthRoleMenuItemRelation.class));
        assertColumn(AuthRoleMenuItemRelation.class, "menuId", "SW_APP_AUTH_MENU_ID", false);
        assertColumn(AuthRoleMenuItemRelation.class, "roleId", "SW_APP_AUTH_ROLE_ID", false);

        assertEquals("SW_SYS_VIEW", tableName(AppSystemView.class));
        assertColumn(AppSystemView.class, "viewId", "VIEW_ID", true);
        assertColumn(AppSystemView.class, "viewName", "VIEW_NAME", false);
        assertColumn(AppSystemView.class, "connectionType", "VIEW_CONTYPE", false);

        assertEquals("SW_SYS_MODULE", tableName(AppInstalledModule.class));
        assertColumn(AppInstalledModule.class, "moduleName", "MODULE_NAME", true);
        assertColumn(AppInstalledModule.class, "remark", "MODULE_REMARK", false);
        assertColumn(AppInstalledModule.class, "assembly", "MODULE_ASSEMBLY", false);
        assertColumn(AppInstalledModule.class, "fileName", "MODULE_FILENAME", false);
        assertColumn(AppInstalledModule.class, "version", "MODULE_VERSION", false);
        assertColumn(AppInstalledModule.class, "generationCode", "MODULE_GENERATIONCODE", false);
        assertColumn(AppInstalledModule.class, "connection", "MODULE_CON", false);

        assertEquals("SW_SYS_MODEL", tableName(AppInstalledModel.class));
        assertColumn(AppInstalledModel.class, "modelId", "MODEL_ID", true);
        assertColumn(AppInstalledModel.class, "modelName", "MODEL_NAME", false);
        assertColumn(AppInstalledModel.class, "className", "MODEL_CLASS", false);
        assertColumn(AppInstalledModel.class, "connectionType", "MODEL_CONTYPE", false);
        assertColumn(AppInstalledModel.class, "tableName", "MODEL_DATABASETABLE", false);
        assertColumn(AppInstalledModel.class, "moduleName", "MODEL_MODULE", false);
        assertColumn(AppInstalledModel.class, "autoSysId", "MODEL_AUTOID", false);
        assertColumn(AppInstalledModel.class, "connection", "MODEL_CON", false);

        assertEquals("SW_SYS_PROPERTY", tableName(AppInstalledProperty.class));
        assertColumn(AppInstalledProperty.class, "propertyId", "SysId", true);
        assertColumn(AppInstalledProperty.class, "propertyType", "PROPERTY_TYPE", false);
        assertColumn(AppInstalledProperty.class, "connectionType", "PROPERTY_CONTYPE", false);
        assertColumn(AppInstalledProperty.class, "name", "PROPERTY_NAME", false);
        assertColumn(AppInstalledProperty.class, "propertyModelId", "PROPERTY_MODEL", false);
        assertColumn(AppInstalledProperty.class, "array", "PROPERTY_ISARRAY", false);
        assertColumn(AppInstalledProperty.class, "columnName", "PROPERTY_COLNAME", false);
        assertColumn(AppInstalledProperty.class, "propertyName", "PROPERTY_PROPERTYNAME", false);
        assertColumn(AppInstalledProperty.class, "multiMap", "PROPERTY_MULTIMAP", false);
        assertColumn(AppInstalledProperty.class, "ixGroup", "PROPERTY_IXGRPOUP", false);
        assertColumn(AppInstalledProperty.class, "check", "PROPERTY_ISCHECK", false);
        assertColumn(AppInstalledProperty.class, "generationType", "PROPERTY_GENERATIONTYPE", false);
        assertColumn(AppInstalledProperty.class, "allowDbNull", "PROPERTY_ALLOWDBNULL", false);
        assertColumn(AppInstalledProperty.class, "canGet", "PROPERTY_CANGET", false);
        assertColumn(AppInstalledProperty.class, "canSet", "PROPERTY_CANSET", false);
        assertColumn(AppInstalledProperty.class, "filter", "PROPERTY_FILTER", false);
        assertColumn(AppInstalledProperty.class, "source", "PROPERTY_SOURCE", false);
        assertColumn(AppInstalledProperty.class, "format", "PROPERTY_FORMAT", false);
        assertColumn(AppInstalledProperty.class, "propertySqlCon", "PROPERTY_SQLCON", false);
        assertColumn(AppInstalledProperty.class, "ownerModelId", "SW_SYS_MODEL_PropertiesSysId", false);

        assertEquals("SW_SYS_RELATION", tableName(AppInstalledRelation.class));
        assertColumn(AppInstalledRelation.class, "relationType", "SW_SYS_RELATION_TYPE", false);
        assertColumn(AppInstalledRelation.class, "sourcePropertyId", "SW_SYS_RELATION_SOURCEPROPERTY", false);
        assertColumn(AppInstalledRelation.class, "targetPropertyId", "SW_SYS_RELATION_TARGETPROPERTY", false);
        assertColumn(AppInstalledRelation.class, "relationTable", "SW_SYS_RELATION_TABLE", false);
        assertColumn(AppInstalledRelation.class, "propertyColumn", "SW_SYS_RELATION_SOURCECOL", false);
        assertColumn(AppInstalledRelation.class, "targetColumn", "SW_SYS_RELATION_TARGETCOL", false);
        assertColumn(AppInstalledRelation.class, "canBeNull", "SW_SYS_RELATION_CANBENULL", false);

        assertEquals(
                Arrays.asList("Web", "WinForm", "Android", "iOS", "Service", "Sensor"),
                Arrays.stream(AppType.values()).map(Enum::name).toList());
    }

    @Test
    public void appFacadeMatchesLegacyAppKeyLookup() {
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("app-1");
        app.setAppKey("secret");

        AppFacade facade = new AppFacade(new AppRepository() {
            @Override
            public ApplicationDefinition findById(String appId) {
                return "app-1".equals(appId) ? app : null;
            }

            @Override
            public List<ApplicationDefinition> findAll() {
                return List.of(app);
            }
        });

        assertSame(app, facade.getApp("app-1", "secret"));
        assertNull(facade.getApp("app-1", "wrong"));
        assertNull(facade.getApp("missing", "secret"));
        assertEquals(List.of(app), facade.getApps());
    }

    @Test
    public void bootstrapPlanCarriesLegacyDefaultMenusAndRole() {
        AppBootstrapPlan plan = AppBootstrapPlan.legacyDefaults();

        assertEquals("系统管理", plan.getSystemMenu().getText());
        assertEquals(
                Arrays.asList("业务包管理", "模型管理", "连接管理", "界面管理", "菜单项管理"),
                plan.getSystemMenu().getSubItems().stream().map(BootstrapMenuItem::getText).toList());

        assertEquals("人员及权限", plan.getAuthMenu().getText());
        assertEquals(
                Arrays.asList("授权用户管理", "部门管理", "角色管理"),
                plan.getAuthMenu().getSubItems().stream().map(BootstrapMenuItem::getText).toList());
        assertEquals("应用管理员", plan.getAdminRoleName());
    }

    @Test
    public void appInstallerExecutesLegacyCreateAppSideEffects() {
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("app-1");
        app.setSysCon("sys-con");
        app.setCreatorId("user-1");
        StoreDatabase database = new StoreDatabase();
        database.setConnection("work-con");
        app.setDataBase(List.of(database));

        RecordingAppInstallGateway gateway = new RecordingAppInstallGateway();
        ApplicationDefinition created = new ApplicationDefinition();
        created.setAppId("created-app");
        gateway.createdApplication = created;

        ApplicationDefinition result = new AppInstaller(gateway, AppBootstrapPlan.legacyDefaults()).createApp(app);

        assertSame(created, result);
        assertEquals(Arrays.asList(
                "createApplication:app-1",
                "installApplicationModules:sys-con",
                "installAuthorizationModules:sys-con",
                "createAuthorizedUser:sys-con:user-1",
                "installUserModules:sys-con:work-con",
                "prepareAppSystemView:sys-con:Module列表",
                "prepareAppSystemView:sys-con:Model列表",
                "prepareAppSystemView:sys-con:SqlCon列表",
                "prepareAppSystemView:sys-con:View列表",
                "prepareAppSystemView:sys-con:MenuItem列表",
                "prepareAppSystemView:sys-con:AuthorizedUser列表",
                "prepareAppSystemView:sys-con:Department列表",
                "prepareAppSystemView:sys-con:Role列表",
                "createMenu:sys-con:系统管理",
                "createMenu:sys-con:人员及权限",
                "createRole:sys-con:应用管理员:user-1:10"),
                gateway.actions);

        BootstrapMenuItem systemMenu = gateway.createdMenus.get(0);
        assertEquals("系统管理", systemMenu.getText());
        assertEquals(Long.valueOf(100), systemMenu.getSubItems().get(0).getViewId());
        assertEquals(Long.valueOf(104), systemMenu.getSubItems().get(4).getViewId());

        BootstrapMenuItem authMenu = gateway.createdMenus.get(1);
        assertEquals("人员及权限", authMenu.getText());
        assertEquals(Long.valueOf(105), authMenu.getSubItems().get(0).getViewId());
        assertEquals(Long.valueOf(107), authMenu.getSubItems().get(2).getViewId());

        BootstrapRole role = gateway.createdRoles.get(0);
        assertEquals("应用管理员", role.getRoleName());
        assertEquals("user-1", role.getAuthorizedUserId());
        assertEquals(Arrays.asList(
                "系统管理",
                "业务包管理",
                "模型管理",
                "连接管理",
                "界面管理",
                "菜单项管理",
                "人员及权限",
                "授权用户管理",
                "部门管理",
                "角色管理"),
                role.getItems().stream().map(BootstrapMenuItem::getText).toList());
    }

    @Test
    public void appInstallerPassesLegacyModelsToSchemaInstallAfterUserModules() {
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("app-1");
        app.setSysCon("sys-con");
        app.setCreatorId("user-1");
        StoreDatabase database = new StoreDatabase();
        database.setConnection("work-con");
        app.setDataBase(List.of(database));
        Model order = legacyModel("Order", "SW_ORDER");
        AppBootstrapPlan plan = AppBootstrapPlan.legacyDefaults();
        plan.setModelSchemas(List.of(order));

        RecordingAppInstallGateway gateway = new RecordingAppInstallGateway();

        new AppInstaller(gateway, plan).createApp(app);

        assertEquals(List.of(order), gateway.installedModelSchemas.get(0));
        assertTrue(gateway.actions.indexOf("installUserModules:sys-con:work-con")
                < gateway.actions.indexOf("installModelSchemas:sys-con:work-con:1"));
        assertTrue(gateway.actions.indexOf("installModelSchemas:sys-con:work-con:1")
                < gateway.actions.indexOf("installDefaultViews:sys-con:work-con:1"));
        assertTrue(gateway.actions.indexOf("installDefaultViews:sys-con:work-con:1")
                < gateway.actions.indexOf("prepareAppSystemView:sys-con:Module列表"));
    }

    @Test
    public void staticModuleSourceOrdersModulesAndFlattensModelsLikeLegacySource() {
        Model baseModel = legacyModel("BaseRecord", "SW_BASE_RECORD");
        Model orderModel = legacyModel("Order", "SW_ORDER");
        AppModuleDefinition baseModule = AppModuleDefinition.legacy(
                "BASE01",
                "example.BaseModule",
                "1.0.0",
                List.of(baseModel));
        AppModuleDefinition orderModule = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "1.0.0",
                List.of(orderModel));
        orderModule.setDependencies(List.of(baseModule));

        StaticAppModuleSource source = new StaticAppModuleSource(List.of(orderModule, baseModule));

        assertEquals(Arrays.asList("BASE01", "MKT01"),
                source.getModules().stream().map(AppModuleDefinition::getName).toList());
        assertEquals(Arrays.asList(baseModel, orderModel), source.getModels());
        assertEquals(List.of(orderModel), source.getModels(orderModule));
    }

    @Test
    public void staticModuleSourceOrdersModelsByLegacyAssemblyDependencies() {
        Model baseModel = legacyModel("BaseRecord", "SW_BASE_RECORD");
        Model orderLine = legacyModel("OrderLine", "SW_ORDER_LINE");
        Model order = legacyModel("Order", "SW_ORDER");
        order.setBaseModel(baseModel);
        Property lines = legacyProperty("lines", "ORDER_ID", PropertyType.BusinessObject);
        lines.setIsCollection(true);
        lines.setPropertyModel(orderLine);
        order.setProperties(List.of(lines));

        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "1.0.0",
                List.of(order, orderLine, baseModel));

        List<Model> ordered = new StaticAppModuleSource(List.of(module)).getModels(module);

        assertTrue(ordered.indexOf(baseModel) < ordered.indexOf(order));
        assertTrue(ordered.indexOf(orderLine) < ordered.indexOf(order));
    }

    @Test
    public void reflectiveModuleSourceCreatesLegacyModelsFromAnnotatedJavaTypes() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "MKT01",
                "example.MarketModule",
                "1.0.0",
                List.of(ReflectiveOrder.class, ReflectiveOrderLine.class, ReflectiveBaseRecord.class));

        AppModuleDefinition module = source.getModules().get(0);
        List<Model> models = source.getModels(module);

        assertEquals("MKT01", module.getName());
        assertEquals("example.MarketModule", module.getRemark());
        assertEquals("MKT01", module.getAssembly());
        assertEquals("MKT01.dll", module.getFileName());
        assertEquals(Arrays.asList(
                        "ReflectiveBaseRecord",
                        "ReflectiveOrderLine",
                        "ReflectiveOrder",
                        "ReflectiveOrderState"),
                models.stream().map(Model::getName).toList());

        Model order = findModel(models, "ReflectiveOrder");
        Model baseRecord = findModel(models, "ReflectiveBaseRecord");
        Model orderLine = findModel(models, "ReflectiveOrderLine");
        Model orderState = findModel(models, "ReflectiveOrderState");
        assertEquals("org.fool.framework.app.AppManageMigrationTest$ReflectiveOrder", order.getClassName());
        assertEquals("RF_ORDER", order.getTableName());
        assertEquals(ModelType.DYNAMIC, order.getModelType());
        assertSame(baseRecord, order.getBaseModel());
        assertEquals("orderId", order.getIdProperty().getName());
        assertEquals("ORDER_ID", order.getIdProperty().getColumn());
        assertEquals(PropertyType.IdentifyId, order.getIdProperty().getPropertyType());
        assertEquals(Boolean.FALSE, order.getIdProperty().getAllowDbNull());

        Property amount = findProperty(order, "amount");
        assertEquals("ORDER_AMOUNT", amount.getColumn());
        assertEquals(PropertyType.Decimal, amount.getPropertyType());

        Property state = findProperty(order, "state");
        assertEquals(PropertyType.Enum, state.getPropertyType());
        assertSame(orderState, state.getPropertyModel());
        assertEquals(ModelType.ENUM, orderState.getModelType());

        Property lines = findProperty(order, "lines");
        assertEquals(Boolean.TRUE, lines.getIsCollection());
        assertEquals(PropertyType.BusinessObject, lines.getPropertyType());
        assertSame(orderLine, lines.getPropertyModel());

        assertEquals(1, order.getRelations().size());
        Relation relation = order.getRelations().get(0);
        assertEquals(RelationType.One2Many, relation.getRelationType());
        assertSame(lines, relation.getProperty());
        assertEquals("RF_ORDER_LINE", relation.getRelationTable());
    }

    @Test
    public void reflectiveModuleSourceIncludesInheritedPropertiesLikeLegacyGetProperties() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "AUDIT",
                "example.AuditModule",
                "1.0.0",
                List.of(ReflectiveInvoice.class, ReflectiveAuditedRecord.class));

        List<Model> models = source.getModels(source.getModules().get(0));
        Model invoice = findModel(models, "ReflectiveInvoice");
        Model auditBase = findModel(models, "ReflectiveAuditedRecord");

        assertSame(auditBase, invoice.getBaseModel());
        assertEquals("invoiceId", invoice.getIdProperty().getName());
        Property createdBy = findProperty(invoice, "createdBy");
        assertEquals("CREATED_BY", createdBy.getColumn());
        assertEquals(PropertyType.String, createdBy.getPropertyType());
    }

    @Test
    public void reflectiveModuleSourceCreatesLegacyRecurveRelationForSelfCollections() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "TREE01",
                "example.TreeModule",
                "1.0.0",
                List.of(ReflectiveTreeNode.class));

        Model node = findModel(source.getModels(source.getModules().get(0)), "ReflectiveTreeNode");
        Property children = findProperty(node, "children");

        assertEquals(Boolean.TRUE, children.getIsCollection());
        assertSame(node, children.getPropertyModel());
        assertEquals(1, node.getRelations().size());
        Relation relation = node.getRelations().get(0);
        assertEquals(RelationType.Recurve, relation.getRelationType());
        assertSame(children, relation.getProperty());
        assertEquals("RF_TREE_NODE_children", relation.getRelationTable());
        assertEquals("RF_TREE_NODE_CHILDREN_ITEM", relation.getPropertyColumn());
        assertEquals("RF_TREE_NODE_childrenNODE_ID", relation.getTargetColumn());
    }

    @Test
    public void reflectiveModuleSourceUsesLegacySysIdTargetColumnForAutoIdSelfCollections() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "TREE02",
                "example.AutoTreeModule",
                "1.0.0",
                List.of(ReflectiveAutoTreeNode.class));

        Model node = findModel(source.getModels(source.getModules().get(0)), "ReflectiveAutoTreeNode");

        assertEquals(Boolean.TRUE, node.getAutoSysId());
        Relation relation = node.getRelations().get(0);
        assertEquals(RelationType.Recurve, relation.getRelationType());
        assertEquals("RF_AUTO_TREE_NODE_children_SYSID", relation.getTargetColumn());
    }

    @Test
    public void reflectiveModuleSourceCreatesLegacyManyToManyRelationForBidirectionalCollections() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "M2M01",
                "example.ManyToManyModule",
                "1.0.0",
                List.of(ReflectiveStudent.class, ReflectiveCourse.class));

        List<Model> models = source.getModels(source.getModules().get(0));
        Model student = findModel(models, "ReflectiveStudent");
        Model course = findModel(models, "ReflectiveCourse");
        Property courses = findProperty(student, "courses");
        Property students = findProperty(course, "students");

        assertEquals(1, student.getRelations().size());
        Relation studentRelation = student.getRelations().get(0);
        assertEquals(RelationType.Many2Many, studentRelation.getRelationType());
        assertSame(courses, studentRelation.getProperty());
        assertSame(students, studentRelation.getTargetProperty());
        assertEquals("RF_COURSE_RF_STUDENT", studentRelation.getRelationTable());
        assertEquals("RF_COURSE_ID", studentRelation.getPropertyColumn());
        assertEquals("RF_STUDENT_ID", studentRelation.getTargetColumn());

        assertEquals(1, course.getRelations().size());
        Relation courseRelation = course.getRelations().get(0);
        assertEquals(RelationType.Many2Many, courseRelation.getRelationType());
        assertSame(students, courseRelation.getProperty());
        assertSame(courses, courseRelation.getTargetProperty());
        assertEquals("RF_COURSE_RF_STUDENT", courseRelation.getRelationTable());
        assertEquals("RF_STUDENT_ID", courseRelation.getPropertyColumn());
        assertEquals("RF_COURSE_ID", courseRelation.getTargetColumn());
    }

    @Test
    public void reflectiveModuleSourceUsesReferToPropertyTargetForCollectionRelations() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "REFTO01",
                "example.ReferToPropertyModule",
                "1.0.0",
                List.of(ReflectiveDepartment.class, ReflectiveDepartmentUser.class));

        List<Model> models = source.getModels(source.getModules().get(0));
        Model department = findModel(models, "ReflectiveDepartment");
        Model user = findModel(models, "ReflectiveDepartmentUser");
        Property users = findProperty(department, "users");
        Property userDepartment = findProperty(user, "department");

        assertEquals(1, department.getRelations().size());
        Relation relation = department.getRelations().get(0);
        assertEquals(RelationType.One2Many, relation.getRelationType());
        assertSame(users, relation.getProperty());
        assertSame(userDepartment, relation.getTargetProperty());
        assertEquals("RF_DEPARTMENT_USER", relation.getRelationTable());
        assertEquals("RF_DEPARTMENT_USER_ID", relation.getPropertyColumn());
        assertEquals("DEPARTMENT_ID", relation.getTargetColumn());
    }

    @Test
    public void reflectiveModuleSourceUsesMultiTypeForCollectionRelationsWithoutReciprocalProperty() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "MULTI01",
                "example.MultiTypeModule",
                "1.0.0",
                List.of(ReflectiveEventDefinition.class, ReflectiveNotificationRole.class));

        Model eventDefinition = findModel(
                source.getModels(source.getModules().get(0)),
                "ReflectiveEventDefinition");
        Property notifyRoles = findProperty(eventDefinition, "notifyRoles");

        assertEquals(1, eventDefinition.getRelations().size());
        Relation relation = eventDefinition.getRelations().get(0);
        assertEquals(RelationType.Many2Many, relation.getRelationType());
        assertSame(notifyRoles, relation.getProperty());
        assertNull(relation.getTargetProperty());
        assertEquals("RF_EVENT_DEF_RF_NOTIFY_ROLE", relation.getRelationTable());
        assertEquals("RF_NOTIFY_ROLE_ID", relation.getPropertyColumn());
        assertEquals("RF_EVENT_DEF_ID", relation.getTargetColumn());
    }

    @Test
    public void reflectiveModuleSourceUsesParentPropertyForObjectWithSubItemRelations() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "SUBITEM01",
                "example.SubItemModule",
                "1.0.0",
                List.of(ReflectiveSubItemOrder.class, ReflectiveSubItemOrderLine.class));

        List<Model> models = source.getModels(source.getModules().get(0));
        Model order = findModel(models, "ReflectiveSubItemOrder");
        Model line = findModel(models, "ReflectiveSubItemOrderLine");
        Property items = findProperty(order, "items");
        Property parent = findProperty(line, "parent");

        assertEquals(1, order.getRelations().size());
        Relation relation = order.getRelations().get(0);
        assertEquals(RelationType.One2Many, relation.getRelationType());
        assertSame(items, relation.getProperty());
        assertSame(parent, relation.getTargetProperty());
        assertEquals("RF_SUBITEM_ORDER_LINE", relation.getRelationTable());
        assertNull(relation.getPropertyColumn());
        assertEquals("PARENT_ORDER_ID", relation.getTargetColumn());
    }

    @Test
    public void appInstallerPersistsReflectiveLegacyColumnMetadata() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "COLMETA01",
                "example.ColumnMetadataModule",
                "1.0.0",
                List.of(ReflectiveColumnMetadataRecord.class));

        gateway.installModuleSource("sys-con", "work-con", source);

        AppInstalledModel installedModel = (AppInstalledModel) daoService.created.get(1);
        AppInstalledProperty code = findCreatedProperty(daoService.created, "code");
        AppInstalledProperty secret = findCreatedProperty(daoService.created, "secret");
        assertEquals(Boolean.TRUE, installedModel.getAutoSysId());
        assertEquals("RECORD_CODE", code.getColumnName());
        assertEquals(PropertyType.MD5, code.getPropertyType());
        assertEquals(Boolean.TRUE, code.getCheck());
        assertEquals("CODE", code.getIxGroup());
        assertEquals(Integer.valueOf(1), code.getGenerationType());
        assertEquals("yyyyMMdd", code.getFormat());
        assertEquals(PropertyType.RadomDECS, secret.getPropertyType());
        assertEquals("SECRET_VALUE", secret.getColumnName());
    }

    @Test
    public void reflectiveModuleSourcePreservesLegacyDefaultValues() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "COLDEFAULT01",
                "example.ColumnDefaultModule",
                "1.0.0",
                List.of(ReflectiveColumnMetadataRecord.class));

        Model model = findModel(source.getModels(source.getModules().get(0)), "ReflectiveColumnMetadataRecord");
        Property status = findProperty(model, "status");

        assertEquals("NEW", status.getDefaultValue());
    }

    @Test
    public void reflectiveModuleSourcePreservesLegacyGenerationExpressions() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "COLEXP01",
                "example.ColumnExpressionModule",
                "1.0.0",
                List.of(ReflectiveColumnMetadataRecord.class));

        Model model = findModel(source.getModels(source.getModules().get(0)), "ReflectiveColumnMetadataRecord");
        Property createdAt = findProperty(model, "createdAt");

        assertEquals("CURRENT_TIMESTAMP", createdAt.getGenerationExpression());
    }

    @Test
    public void reflectiveModuleSourcePreservesLegacyNoMapAndMultiColumnDbMaps() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "DBMAP01",
                "example.DbMapModule",
                "1.0.0",
                List.of(ReflectiveMultiMapOrder.class, ReflectiveCustomerSnapshot.class));

        List<Model> models = source.getModels(source.getModules().get(0));
        Model order = findModel(models, "ReflectiveMultiMapOrder");
        Property transientNote = findProperty(order, "transientNote");
        Property customer = findProperty(order, "customer");

        assertNull(transientNote.getColumn());
        assertEquals(Boolean.FALSE, transientNote.getMultiMap());
        assertEquals(PropertyType.BusinessObject, customer.getPropertyType());
        assertEquals(Boolean.TRUE, customer.getMultiMap());
        assertNull(customer.getColumn());
        assertSame(findModel(models, "ReflectiveCustomerSnapshot"), customer.getPropertyModel());
        assertEquals(2, customer.getDbMaps().size());
        assertEquals("customerId", customer.getDbMaps().get(0).getPropertyName());
        assertEquals("CUSTOMER_ID", customer.getDbMaps().get(0).getColumnName());
        assertEquals("displayName", customer.getDbMaps().get(1).getPropertyName());
        assertEquals("CUSTOMER_NAME", customer.getDbMaps().get(1).getColumnName());
    }

    @Test
    public void reflectiveModuleSourceAppliesLegacyTableColumnPrefixes() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "PREFIX01",
                "example.PrefixModule",
                "1.0.0",
                List.of(ReflectivePrefixedOrder.class, ReflectiveCustomerSnapshot.class));

        List<Model> models = source.getModels(source.getModules().get(0));
        Model order = findModel(models, "ReflectivePrefixedOrder");
        Property orderId = findProperty(order, "orderId");
        Property status = findProperty(order, "status");
        Property customer = findProperty(order, "customer");

        assertEquals("TENANT_ORDER_ID", orderId.getColumn());
        assertEquals("ORDER_STATUS", status.getColumn());
        assertEquals(Boolean.TRUE, customer.getMultiMap());
        assertNull(customer.getColumn());
        assertEquals(2, customer.getDbMaps().size());
        assertEquals("customerId", customer.getDbMaps().get(0).getPropertyName());
        assertEquals("TENANT_LONG_CUSTOMER_ID", customer.getDbMaps().get(0).getColumnName());
        assertEquals("displayName", customer.getDbMaps().get(1).getPropertyName());
        assertEquals("CUSTOMER_NAME", customer.getDbMaps().get(1).getColumnName());
    }

    @Test
    public void reflectiveModuleSourceScansAnnotatedModelsFromPackageLikeAssemblySource() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "PKG01",
                "example.PackageModule",
                "1.0.0",
                "org.fool.framework.app.reflective",
                Thread.currentThread().getContextClassLoader());

        AppModuleDefinition module = source.getModules().get(0);
        List<Model> models = source.getModels(module);

        assertEquals(Arrays.asList(
                        "PackageBaseRecord",
                        "PackageOrderLine",
                        "PackageOrder",
                        "PackageOrderState"),
                models.stream().map(Model::getName).toList());
        Model order = findModel(models, "PackageOrder");
        Model state = findModel(models, "PackageOrderState");
        assertEquals(
                "org.fool.framework.app.reflective.PackageScanFixtures$PackageOrder",
                order.getClassName());
        assertEquals("PKG_ORDER", order.getTableName());
        assertEquals(ModelType.ENUM, state.getModelType());
        assertEquals(Arrays.asList("OPEN:0", "CLOSED:1"),
                enumValuePairs(state));
        assertTrue(models.indexOf(findModel(models, "PackageBaseRecord")) < models.indexOf(order));
        assertTrue(models.indexOf(findModel(models, "PackageOrderLine")) < models.indexOf(order));
        assertSame(state, findProperty(order, "state").getPropertyModel());
    }

    @Test
    public void reflectiveModuleSourceCreatesReferencedPackageModulesLikeLegacyAssemblyFactory() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "REF_ORDER",
                "example.ReferenceOrderModule",
                "1.0.0",
                "org.fool.framework.app.reference.order",
                Thread.currentThread().getContextClassLoader());

        List<AppModuleDefinition> modules = source.getModules();

        assertEquals(Arrays.asList(
                        ReferenceCustomer.class.getPackageName(),
                        "REF_ORDER"),
                modules.stream().map(AppModuleDefinition::getName).toList());
        assertEquals(1, modules.get(1).getDependencies().size());
        assertSame(modules.get(0), modules.get(1).getDependencies().get(0));

        List<Model> sharedModels = source.getModels(modules.get(0));
        List<Model> orderModels = source.getModels(modules.get(1));
        Model customer = findModel(sharedModels, "ReferenceCustomer");
        Model order = findModel(orderModels, "ReferenceOrder");

        assertEquals("REF_CUSTOMER", customer.getTableName());
        assertEquals("REF_ORDER", order.getTableName());
        assertSame(customer, findProperty(order, "customer").getPropertyModel());
        assertEquals(PropertyType.BusinessObject, findProperty(order, "customer").getPropertyType());
    }

    @Test
    public void appInstallerLoadsLegacyModelsFromConfiguredModuleSource() {
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("app-1");
        app.setSysCon("sys-con");
        app.setCreatorId("user-1");
        StoreDatabase database = new StoreDatabase();
        database.setConnection("work-con");
        app.setDataBase(List.of(database));
        Model baseModel = legacyModel("BaseRecord", "SW_BASE_RECORD");
        Model orderModel = legacyModel("Order", "SW_ORDER");
        AppModuleDefinition baseModule = AppModuleDefinition.legacy(
                "BASE01",
                "example.BaseModule",
                "1.0.0",
                List.of(baseModel));
        AppModuleDefinition orderModule = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "1.0.0",
                List.of(orderModel));
        orderModule.setDependencies(List.of(baseModule));
        AppBootstrapPlan plan = AppBootstrapPlan.legacyDefaults();
        plan.setModelModuleSource(new StaticAppModuleSource(List.of(orderModule, baseModule)));

        RecordingAppInstallGateway gateway = new RecordingAppInstallGateway();

        new AppInstaller(gateway, plan).createApp(app);

        assertEquals(Arrays.asList(baseModel, orderModel), gateway.installedModelSchemas.get(0));
        assertEquals(Arrays.asList(baseModel, orderModel), gateway.installedDefaultViews.get(0));
        assertTrue(gateway.actions.indexOf("installUserModules:sys-con:work-con")
                < gateway.actions.indexOf("installModelSchemas:sys-con:work-con:2"));
        assertTrue(gateway.actions.indexOf("installDefaultViews:sys-con:work-con:2")
                < gateway.actions.indexOf("prepareAppSystemView:sys-con:Module列表"));
    }

    @Test
    public void appInstallerInstallsModuleSourceMetadataBeforeModelSchemas() {
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("app-1");
        app.setSysCon("sys-con");
        app.setCreatorId("user-1");
        StoreDatabase database = new StoreDatabase();
        database.setConnection("work-con");
        app.setDataBase(List.of(database));
        Model orderModel = legacyModel("Order", "SW_ORDER");
        AppModuleDefinition orderModule = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "1.0.0",
                List.of(orderModel));
        AppBootstrapPlan plan = AppBootstrapPlan.legacyDefaults();
        plan.setModelModuleSource(new StaticAppModuleSource(List.of(orderModule)));

        RecordingAppInstallGateway gateway = new RecordingAppInstallGateway();

        new AppInstaller(gateway, plan).createApp(app);

        assertEquals(List.of(orderModule), gateway.installedModuleSources.get(0).getModules());
        assertTrue(gateway.actions.indexOf("installUserModules:sys-con:work-con")
                < gateway.actions.indexOf("installModuleSource:sys-con:work-con:1"));
        assertTrue(gateway.actions.indexOf("installModuleSource:sys-con:work-con:1")
                < gateway.actions.indexOf("installModelSchemas:sys-con:work-con:1"));
    }

    @Test
    public void daoAppInstallGatewayPersistsApplicationAndLegacySideEffects() throws Exception {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        ApplicationDefinition app = new ApplicationDefinition();
        app.setAppId("app-1");

        ApplicationDefinition result = gateway.createApplication(app);

        assertSame(app, result);
        assertEquals(List.of(app), daoService.created);
        gateway.createAuthorizedUser("sys-con", "user-1");
        assertEquals(2, daoService.created.size());
        AuthorizedUser user = (AuthorizedUser) daoService.created.get(1);
        assertEquals("user-1", user.getUserId());
        assertEquals("user-1", user.getUserLoginName());
        assertNull(user.getDepartmentId());
        BootstrapMenuItem menu = new BootstrapMenuItem("系统管理");
        BootstrapMenuItem modelMenu = menu.addSubItem("模型管理", "Model列表").getSubItems().get(0);
        modelMenu.setViewId(101L);
        BootstrapMenuItem viewMenu = menu.addSubItem("界面管理", "View列表").getSubItems().get(1);
        viewMenu.setViewId(102L);

        gateway.createMenu("sys-con", menu);

        assertEquals(7, daoService.created.size());
        AuthMenuItem root = (AuthMenuItem) daoService.created.get(2);
        AuthMenuItem first = (AuthMenuItem) daoService.created.get(3);
        AuthMenuSubItemRelation firstRelation = (AuthMenuSubItemRelation) daoService.created.get(4);
        AuthMenuItem second = (AuthMenuItem) daoService.created.get(5);
        AuthMenuSubItemRelation secondRelation = (AuthMenuSubItemRelation) daoService.created.get(6);
        assertEquals("系统管理", root.getText());
        assertEquals(Long.valueOf(0), root.getViewId());
        assertEquals("模型管理", first.getText());
        assertEquals(Long.valueOf(101), first.getViewId());
        assertEquals("界面管理", second.getText());
        assertEquals(Long.valueOf(102), second.getViewId());
        assertEquals(root.getMenuId(), firstRelation.getParentMenuId());
        assertEquals(first.getMenuId(), firstRelation.getSubItemMenuId());
        assertEquals(root.getMenuId(), secondRelation.getParentMenuId());
        assertEquals(second.getMenuId(), secondRelation.getSubItemMenuId());
        assertEquals(root.getMenuId(), menu.getPersistedId());
        assertEquals(first.getMenuId(), modelMenu.getPersistedId());
        assertEquals(second.getMenuId(), viewMenu.getPersistedId());

        BootstrapRole role = new BootstrapRole("应用管理员", "user-1");
        role.getItems().add(menu);
        role.getItems().addAll(menu.getSubItems());
        gateway.createRole("sys-con", role);

        assertEquals(12, daoService.created.size());
        AuthRole createdRole = (AuthRole) daoService.created.get(7);
        AuthRoleAuthorizedUserRelation userRelation = (AuthRoleAuthorizedUserRelation) daoService.created.get(8);
        AuthRoleMenuItemRelation rootRoleRelation = (AuthRoleMenuItemRelation) daoService.created.get(9);
        AuthRoleMenuItemRelation firstRoleRelation = (AuthRoleMenuItemRelation) daoService.created.get(10);
        AuthRoleMenuItemRelation secondRoleRelation = (AuthRoleMenuItemRelation) daoService.created.get(11);
        assertEquals("应用管理员", createdRole.getRoleName());
        assertEquals(createdRole.getRoleId(), userRelation.getRoleId());
        assertEquals(user.getAuthorizedId(), userRelation.getAuthorizedUserId());
        assertEquals(root.getMenuId(), rootRoleRelation.getMenuId());
        assertEquals(createdRole.getRoleId(), rootRoleRelation.getRoleId());
        assertEquals(first.getMenuId(), firstRoleRelation.getMenuId());
        assertEquals(createdRole.getRoleId(), firstRoleRelation.getRoleId());
        assertEquals(second.getMenuId(), secondRoleRelation.getMenuId());
        assertEquals(createdRole.getRoleId(), secondRoleRelation.getRoleId());

        Long viewId = gateway.prepareAppSystemView("sys-con", "Model列表");

        assertEquals(13, daoService.created.size());
        AppSystemView view = (AppSystemView) daoService.created.get(12);
        assertEquals(view.getViewId(), viewId);
        assertEquals("Model列表", view.getViewName());
        assertEquals(Integer.valueOf(AppSystemView.CONNECTION_TYPE_APP_SYS), view.getConnectionType());
        assertNotNull(DaoAppInstallGateway.class.getDeclaredAnnotation(Component.class));
        assertNotNull(DaoAppInstallGateway.class
                .getConstructor(DaoService.class, AppDaoServiceFactory.class)
                .getDeclaredAnnotation(Autowired.class));

        gateway.installApplicationModules("sys-con");
        gateway.installAuthorizationModules("sys-con");
        gateway.installUserModules("sys-con", "work-con");

        assertEquals(19, daoService.created.size());
        AppInstalledModule applicationModule = (AppInstalledModule) daoService.created.get(13);
        AppInstalledModel applicationModel = (AppInstalledModel) daoService.created.get(14);
        AppInstalledModule authorizationModule = (AppInstalledModule) daoService.created.get(15);
        AppInstalledModel authorizationModel = (AppInstalledModel) daoService.created.get(16);
        AppInstalledModule userModule = (AppInstalledModule) daoService.created.get(17);
        AppInstalledModel userModel = (AppInstalledModel) daoService.created.get(18);
        assertInstalledModule(applicationModule, "SCPB07", "Soway.Model.App.Application", "1.0.1605.2401", "sys-con");
        assertInstalledModule(authorizationModule, "SWUA02", "SOWAY.ORM.AUTH.AuthorizedUser", "1.0.16045.3001", "sys-con");
        assertInstalledModule(userModule, "SWUA01", "SOWAY.ORM.AUTH.User", "1.0.16015.3001", "work-con");
        assertInstalledModel(
                applicationModel,
                "Application",
                "Soway.Model.App.Application",
                "SW_APPLICATION",
                "SCPB07",
                AppInstalledModel.CONNECTION_TYPE_APP_SYS,
                "sys-con");
        assertInstalledModel(
                authorizationModel,
                "AuthorizedUser",
                "SOWAY.ORM.AUTH.AuthorizedUser",
                "SW_APP_AUTH_USER",
                "SWUA02",
                AppInstalledModel.CONNECTION_TYPE_APP_SYS,
                "sys-con");
        assertInstalledModel(
                userModel,
                "User",
                "SOWAY.ORM.AUTH.User",
                "SW_AUTH_USER",
                "SWUA01",
                AppInstalledModel.CONNECTION_TYPE_CURRENT,
                "work-con");
    }

    @Test
    public void daoAppInstallGatewayPersistsLegacyModuleSourceMetadata() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = legacyModel("Order", "SW_ORDER");
        order.setAutoSysId(true);
        Model state = legacyModel("OrderState", "SW_ORDER_STATE");
        state.setModelType(ModelType.ENUM);
        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "2.0.0",
                List.of(order, state));

        List<String> installed = gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(module)));

        assertEquals(Arrays.asList("MKT01", "Order", "OrderState"), installed);
        assertEquals(3, daoService.created.size());
        AppInstalledModule installedModule = (AppInstalledModule) daoService.created.get(0);
        AppInstalledModel orderModel = (AppInstalledModel) daoService.created.get(1);
        AppInstalledModel enumModel = (AppInstalledModel) daoService.created.get(2);
        assertInstalledModule(installedModule, "MKT01", "example.OrderModule", "2.0.0", "work-con");
        assertInstalledModel(
                orderModel,
                "Order",
                "example.Order",
                "SW_ORDER",
                "MKT01",
                AppInstalledModel.CONNECTION_TYPE_CURRENT,
                "work-con",
                Boolean.TRUE);
        assertInstalledModel(
                enumModel,
                "OrderState",
                "example.OrderState",
                "SW_ORDER_STATE",
                "MKT01",
                AppInstalledModel.CONNECTION_TYPE_CURRENT,
                "work-con");
    }

    @Test
    public void daoAppInstallGatewayPersistsLegacyEnumValuesWithEnumModels() throws Exception {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model state = legacyModel("OrderState", null);
        state.setModelType(ModelType.ENUM);
        state.setEnumValues(List.of(enumValue("OPEN", "0"), enumValue("CLOSED", "1")));
        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "2.0.0",
                List.of(state));

        gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(module)));

        AppInstalledModel enumModel = (AppInstalledModel) daoService.created.get(1);
        List<Object> enumValues = createdBySimpleName(daoService.created, "AppInstalledEnumValue");
        assertEquals(2, enumValues.size());
        assertEquals("SW_SYS_EMUNVALUE", tableName(enumValues.get(0).getClass()));
        assertColumn(enumValues.get(0).getClass(), "name", "EMUN_STR", false);
        assertColumn(enumValues.get(0).getClass(), "value", "EMUN_VALUE", false);
        assertColumn(enumValues.get(0).getClass(), "ownerModelId", "SW_SYS_MODEL_EnumValuesMODEL_ID", false);
        assertInstalledEnumValue(enumValues.get(0), "OPEN", 0, enumModel.getModelId());
        assertInstalledEnumValue(enumValues.get(1), "CLOSED", 1, enumModel.getModelId());
    }

    @Test
    public void daoAppInstallGatewayPersistsLegacyModuleSourceProperties() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = legacyModel("Order", "SW_ORDER");
        Property orderId = legacyProperty("orderId", "ORDER_ID", PropertyType.IdentifyId);
        Property status = legacyProperty("status", "ORDER_STATUS", PropertyType.String);
        status.setAllowDbNull(true);
        status.setCheck(false);
        status.setIxGroup("STATUS");
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId, status));
        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "2.0.0",
                List.of(order));

        gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(module)));

        assertEquals(4, daoService.created.size());
        AppInstalledModel orderModel = (AppInstalledModel) daoService.created.get(1);
        AppInstalledProperty orderIdProperty = (AppInstalledProperty) daoService.created.get(2);
        AppInstalledProperty statusProperty = (AppInstalledProperty) daoService.created.get(3);
        assertInstalledProperty(
                orderIdProperty,
                "orderId",
                "orderId",
                "ORDER_ID",
                PropertyType.IdentifyId,
                AppInstalledModel.CONNECTION_TYPE_CURRENT,
                false,
                false,
                true,
                "",
                orderModel.getModelId(),
                null,
                "work-con");
        assertInstalledProperty(
                statusProperty,
                "status",
                "status",
                "ORDER_STATUS",
                PropertyType.String,
                AppInstalledModel.CONNECTION_TYPE_CURRENT,
                false,
                true,
                false,
                "STATUS",
                orderModel.getModelId(),
                null,
                "work-con");
    }

    @Test
    public void daoAppInstallGatewayPersistsLegacyModuleSourceRelations() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = legacyModel("Order", "SW_ORDER");
        Model orderLine = legacyModel("OrderLine", "SW_ORDER_LINE");
        Property orderId = legacyProperty("orderId", "ORDER_ID", PropertyType.IdentifyId);
        Property lines = legacyProperty("lines", "ORDER_ID", PropertyType.BusinessObject);
        lines.setPropertyModel(orderLine);
        lines.setIsCollection(true);
        Property lineOrderId = legacyProperty("lineOrderId", "LINE_ORDER_ID", PropertyType.Long);
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId, lines));
        orderLine.setProperties(List.of(lineOrderId));

        Relation relation = new Relation();
        relation.setRelationType(RelationType.One2Many);
        relation.setProperty(lines);
        relation.setTargetProperty(lineOrderId);
        relation.setRelationTable("SW_ORDER_LINE");
        relation.setPropertyColumn("ORDER_ID");
        relation.setTargetColumn("LINE_ORDER_ID");
        relation.setCanBeNull(true);
        order.setRelations(List.of(relation));

        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "2.0.0",
                List.of(order, orderLine));

        gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(module)));

        assertEquals(7, daoService.created.size());
        AppInstalledProperty linesProperty = findCreatedProperty(daoService.created, "lines");
        AppInstalledProperty lineOrderIdProperty = findCreatedProperty(daoService.created, "lineOrderId");
        AppInstalledRelation installedRelation = (AppInstalledRelation) daoService.created.get(6);
        assertInstalledRelation(
                installedRelation,
                RelationType.One2Many,
                linesProperty.getPropertyId(),
                lineOrderIdProperty.getPropertyId(),
                "SW_ORDER_LINE",
                "ORDER_ID",
                "LINE_ORDER_ID",
                true);
    }

    @Test
    public void daoAppInstallGatewayExecutesLegacyModelAndRelationDdl() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = legacyModel("Order", "SW_ORDER");
        Property orderId = legacyProperty("id", "ORDER_ID", PropertyType.IdentifyId);
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId));

        Relation relation = new Relation();
        relation.setRelationType(RelationType.One2Many);
        relation.setRelationTable("SW_ORDER_LINE");
        relation.setTargetColumn("ORDER_ID");
        order.setRelations(List.of(relation));

        List<String> executed = gateway.installModelSchemas("sys-con", "work-con", List.of(order));

        assertEquals(2, executed.size());
        assertEquals(executed, daoService.executedSql);
        assertTrue(executed.get(0).contains("CREATE TABLE IF NOT EXISTS `SW_ORDER`"));
        assertTrue(executed.get(1).contains("ALTER TABLE `SW_ORDER_LINE` ADD COLUMN `ORDER_ID` BIGINT NULL"));
    }

    @Test
    public void daoAppInstallGatewayRoutesLegacyMetadataAndDdlToSeparateConnections() {
        RecordingDaoService sysDao = new RecordingDaoService();
        RecordingDaoService workDao = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(
                connection -> "work-con".equals(connection) ? workDao : sysDao);
        Model order = legacyModel("Order", "SW_ORDER");
        order.setProperties(List.of(legacyProperty("id", "ORDER_ID", PropertyType.IdentifyId)));
        AppModuleDefinition module = AppModuleDefinition.legacy(
                "MKT01",
                "example.OrderModule",
                "2.0.0",
                List.of(order));

        gateway.installModuleSource("sys-con", "work-con", new StaticAppModuleSource(List.of(module)));
        gateway.installModelSchemas("sys-con", "work-con", List.of(order));
        gateway.installDefaultViews("sys-con", "work-con", List.of(order));

        assertEquals(7, sysDao.created.size());
        assertTrue(sysDao.created.get(0) instanceof AppInstalledModule);
        assertTrue(sysDao.created.get(1) instanceof AppInstalledModel);
        assertTrue(sysDao.created.get(2) instanceof AppInstalledProperty);
        assertTrue(sysDao.created.get(3) instanceof View);
        assertTrue(sysDao.created.get(4) instanceof ViewItem);
        assertTrue(sysDao.executedSql.isEmpty());
        assertTrue(workDao.created.isEmpty());
        assertEquals(1, workDao.executedSql.size());
        assertTrue(workDao.executedSql.get(0).contains("CREATE TABLE IF NOT EXISTS `SW_ORDER`"));
    }

    @Test
    public void daoAppInstallGatewaySpringConstructorRoutesConnectionStringsThroughFactory() {
        RecordingDaoService defaultDao = new RecordingDaoService();
        RecordingDaoService sysDao = new RecordingDaoService();
        RecordingDaoService workDao = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(
                defaultDao,
                connection -> "work-con".equals(connection) ? workDao : sysDao);
        Model order = legacyModel("Order", "SW_ORDER");
        order.setProperties(List.of(legacyProperty("id", "ORDER_ID", PropertyType.IdentifyId)));

        gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(AppModuleDefinition.legacy(
                        "MKT01",
                        "example.OrderModule",
                        "2.0.0",
                        List.of(order)))));
        gateway.installModelSchemas("sys-con", "work-con", List.of(order));

        assertEquals(3, sysDao.created.size());
        assertEquals(1, workDao.executedSql.size());
        assertTrue(defaultDao.created.isEmpty());
        assertTrue(defaultDao.executedSql.isEmpty());
    }

    @Test
    public void driverManagerAppDaoServiceFactoryParsesLegacyConnectionStringsAndCachesDaos() {
        DriverManagerAppDaoServiceFactory.ConnectionSettings settings =
                DriverManagerAppDaoServiceFactory.parse(
                        "Url=jdbc:mysql://db:3306/appdb;User ID=app_user;Password=secret;"
                                + "DriverClassName=com.mysql.cj.jdbc.Driver");
        assertEquals("jdbc:mysql://db:3306/appdb", settings.url());
        assertEquals("app_user", settings.username());
        assertEquals("secret", settings.password());
        assertEquals("com.mysql.cj.jdbc.Driver", settings.driverClassName());

        DriverManagerAppDaoServiceFactory factory =
                new DriverManagerAppDaoServiceFactory(new org.fool.framework.dao.SqlScriptGenerator());
        DaoService first = factory.create("jdbc:mysql://db:3306/appdb");
        DaoService second = factory.create("jdbc:mysql://db:3306/appdb");

        assertSame(first, second);
        assertNotNull(first);
        assertNotNull(DriverManagerAppDaoServiceFactory.class.getDeclaredAnnotation(Component.class));
    }

    @Test
    public void driverManagerAppDaoServiceFactoryParsesLegacySqlConStrings() {
        DriverManagerAppDaoServiceFactory.ConnectionSettings settings =
                DriverManagerAppDaoServiceFactory.parse(
                        "Data Source=legacy-db;Initial Catalog=LegacyApp;"
                                + "Integrated Security=False;User ID=app_user;Password=secret");

        assertEquals("jdbc:sqlserver://legacy-db;databaseName=LegacyApp", settings.url());
        assertEquals("app_user", settings.username());
        assertEquals("secret", settings.password());
        assertNull(settings.driverClassName());
    }

    @Test
    public void daoAppInstallGatewayPersistsLegacyDefaultViewsForModels() {
        RecordingDaoService daoService = new RecordingDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = legacyModel("Order", "SW_ORDER");
        order.setProperties(List.of(
                legacyProperty("orderId", "ORDER_ID", PropertyType.IdentifyId),
                legacyProperty("symbol", "ORDER_SYMBOL", PropertyType.String)));

        List<String> viewNames = gateway.installDefaultViews("sys-con", "work-con", List.of(order));

        assertEquals(Arrays.asList("Order详细", "Order列表"), viewNames);
        assertEquals(6, daoService.created.size());
        View detailView = (View) daoService.created.get(0);
        ViewItem detailFirstItem = (ViewItem) daoService.created.get(1);
        ViewItem detailSecondItem = (ViewItem) daoService.created.get(2);
        View listView = (View) daoService.created.get(3);
        ViewItem listFirstItem = (ViewItem) daoService.created.get(4);
        ViewItem listSecondItem = (ViewItem) daoService.created.get(5);
        assertEquals("Order详细", detailView.getViewName());
        assertEquals("Order列表", listView.getViewName());
        assertEquals(detailView.getId(), detailFirstItem.getViewId());
        assertEquals(detailView.getId(), detailSecondItem.getViewId());
        assertEquals(listView.getId(), listFirstItem.getViewId());
        assertEquals(listView.getId(), listSecondItem.getViewId());
        assertTrue(detailFirstItem.isCanEdit());
        assertTrue(!listFirstItem.isCanEdit());
    }

    private static String tableName(Class<?> type) {
        Table table = type.getDeclaredAnnotation(Table.class);
        assertNotNull("missing @Table on " + type.getName(), table);
        return table.value();
    }

    private static void assertColumn(Class<?> type, String fieldName, String columnName, boolean key) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull("missing @Column on " + fieldName, column);
        assertEquals(columnName, column.value());
        assertEquals(key, field.getDeclaredAnnotation(Id.class) != null);
        assertTrue(field.getType() != Void.class);
    }

    private static void assertInstalledModule(
            AppInstalledModule module,
            String moduleName,
            String sourceType,
            String version,
            String connection) {
        assertEquals(moduleName, module.getModuleName());
        assertEquals(sourceType, module.getRemark());
        assertEquals(moduleName, module.getAssembly());
        assertEquals(moduleName + ".dll", module.getFileName());
        assertEquals(version, module.getVersion());
        assertEquals(Boolean.TRUE, module.getGenerationCode());
        assertEquals(connection, module.getConnection());
    }

    private static void assertInstalledModel(
            AppInstalledModel model,
            String modelName,
            String className,
                String tableName,
                String moduleName,
                Integer connectionType,
                String connection) {
        assertInstalledModel(model, modelName, className, tableName, moduleName, connectionType, connection, Boolean.FALSE);
    }

    private static void assertInstalledModel(
            AppInstalledModel model,
            String modelName,
            String className,
            String tableName,
            String moduleName,
            Integer connectionType,
            String connection,
            Boolean autoSysId) {
        assertNotNull(model.getModelId());
        assertEquals(modelName, model.getModelName());
        assertEquals(className, model.getClassName());
        assertEquals(tableName, model.getTableName());
        assertEquals(moduleName, model.getModuleName());
        assertEquals(connectionType, model.getConnectionType());
        assertEquals(autoSysId, model.getAutoSysId());
        assertEquals(connection, model.getConnection());
    }

    private static void assertInstalledProperty(
            AppInstalledProperty property,
            String name,
            String propertyName,
            String columnName,
            PropertyType propertyType,
            Integer connectionType,
            boolean array,
            boolean allowDbNull,
            boolean check,
            String ixGroup,
            Long ownerModelId,
            Long propertyModelId,
            String propertySqlCon) {
        assertNotNull(property.getPropertyId());
        assertEquals(name, property.getName());
        assertEquals(propertyName, property.getPropertyName());
        assertEquals(columnName, property.getColumnName());
        assertEquals(propertyType, property.getPropertyType());
        assertEquals(connectionType, property.getConnectionType());
        assertEquals(array, property.getArray());
        assertEquals(allowDbNull, property.getAllowDbNull());
        assertEquals(check, property.getCheck());
        assertEquals(ixGroup, property.getIxGroup());
        assertEquals(ownerModelId, property.getOwnerModelId());
        assertEquals(propertyModelId, property.getPropertyModelId());
        assertEquals(Boolean.TRUE, property.getCanGet());
        assertEquals(Boolean.TRUE, property.getCanSet());
        assertEquals(propertySqlCon, property.getPropertySqlCon());
    }

    private static void assertInstalledRelation(
            AppInstalledRelation relation,
            RelationType relationType,
            Long sourcePropertyId,
            Long targetPropertyId,
            String relationTable,
            String propertyColumn,
            String targetColumn,
            boolean canBeNull) {
        assertEquals(relationType, relation.getRelationType());
        assertEquals(sourcePropertyId, relation.getSourcePropertyId());
        assertEquals(targetPropertyId, relation.getTargetPropertyId());
        assertEquals(relationTable, relation.getRelationTable());
        assertEquals(propertyColumn, relation.getPropertyColumn());
        assertEquals(targetColumn, relation.getTargetColumn());
        assertEquals(canBeNull, relation.getCanBeNull());
    }

    private static AppInstalledProperty findCreatedProperty(List<Object> created, String propertyName) {
        return created.stream()
                .filter(AppInstalledProperty.class::isInstance)
                .map(AppInstalledProperty.class::cast)
                .filter(property -> propertyName.equals(property.getPropertyName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing property " + propertyName));
    }

    private static List<Object> createdBySimpleName(List<Object> created, String simpleName) {
        return created.stream()
                .filter(object -> simpleName.equals(object.getClass().getSimpleName()))
                .toList();
    }

    private static void assertInstalledEnumValue(
            Object enumValue,
            String name,
            Integer value,
            Long ownerModelId) throws Exception {
        assertEquals(name, enumValue.getClass().getMethod("getName").invoke(enumValue));
        assertEquals(value, enumValue.getClass().getMethod("getValue").invoke(enumValue));
        assertEquals(ownerModelId, enumValue.getClass().getMethod("getOwnerModelId").invoke(enumValue));
    }

    private static Model findModel(List<Model> models, String name) {
        return models.stream()
                .filter(model -> name.equals(model.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing model " + name));
    }

    private static Property findProperty(Model model, String name) {
        return model.getProperties().stream()
                .filter(property -> name.equals(property.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing property " + name));
    }

    private static Model legacyModel(String name, String tableName) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName(tableName);
        model.setAutoSysId(false);
        return model;
    }

    private static Property legacyProperty(String name, String column, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        property.setPropertyType(type);
        property.setAllowDbNull(false);
        property.setCheck(true);
        property.setIxGroup("");
        property.setIsCollection(false);
        return property;
    }

    @SuppressWarnings("unchecked")
    private static List<String> enumValuePairs(Model model) {
        try {
            Field field = Model.class.getDeclaredField("enumValues");
            field.setAccessible(true);
            List<EnumValue> values = (List<EnumValue>) field.get(model);
            if (values == null) {
                return List.of();
            }
            return values.stream()
                    .map(value -> value.getName() + ":" + value.getValue())
                    .toList();
        } catch (ReflectiveOperationException ex) {
            return List.of();
        }
    }

    @Table("RF_BASE_RECORD")
    private static class ReflectiveBaseRecord {
        @Id
        @Column("BASE_ID")
        private Long baseId;
    }

    @Table("RF_AUDITED_RECORD")
    private static class ReflectiveAuditedRecord {
        @Column("CREATED_BY")
        private String createdBy;
    }

    @Table("RF_INVOICE")
    private static class ReflectiveInvoice extends ReflectiveAuditedRecord {
        @Id
        @Column("INVOICE_ID")
        private Long invoiceId;
    }

    @Table("RF_ORDER_LINE")
    private static class ReflectiveOrderLine {
        @Id
        @Column("LINE_ID")
        private Long lineId;
        @Column("LINE_SKU")
        private String sku;
    }

    @Table("RF_ORDER")
    private static class ReflectiveOrder extends ReflectiveBaseRecord {
        @Id
        @Column("ORDER_ID")
        private Long orderId;
        @Column("ORDER_AMOUNT")
        private BigDecimal amount;
        @Column("ORDER_STATE")
        private ReflectiveOrderState state;
        private List<ReflectiveOrderLine> lines;
    }

    private enum ReflectiveOrderState {
        OPEN,
        CLOSED
    }

    @Table("RF_TREE_NODE")
    private static class ReflectiveTreeNode {
        @Id
        @Column("NODE_ID")
        private Long nodeId;
        private List<ReflectiveTreeNode> children;
    }

    @Table("RF_AUTO_TREE_NODE")
    private static class ReflectiveAutoTreeNode {
        private String name;
        private List<ReflectiveAutoTreeNode> children;
    }

    @Table("RF_STUDENT")
    private static class ReflectiveStudent {
        @Id
        @Column("STUDENT_ID")
        private Long studentId;
        private List<ReflectiveCourse> courses;
    }

    @Table("RF_COURSE")
    private static class ReflectiveCourse {
        @Id
        @Column("COURSE_ID")
        private Long courseId;
        private List<ReflectiveStudent> students;
    }

    @Table("RF_DEPARTMENT")
    private static class ReflectiveDepartment {
        @Id
        @Column("DEPARTMENT_ID")
        private Long departmentId;
        @ReferToProperty("department")
        private List<ReflectiveDepartmentUser> users;
    }

    @Table("RF_DEPARTMENT_USER")
    private static class ReflectiveDepartmentUser {
        @Id
        @Column("USER_ID")
        private Long userId;
        @Column("DEPARTMENT_ID")
        private ReflectiveDepartment department;
    }

    @Table("RF_EVENT_DEF")
    private static class ReflectiveEventDefinition {
        @Id
        @Column("DEF_ID")
        private Long defId;
        @MultiType
        private List<ReflectiveNotificationRole> notifyRoles;
    }

    @Table("RF_NOTIFY_ROLE")
    private static class ReflectiveNotificationRole {
        @Id
        @Column("ROLE_ID")
        private Long roleId;
    }

    @Table("RF_SUBITEM_ORDER")
    private static class ReflectiveSubItemOrder extends ObjectWithSubItem<ReflectiveSubItemOrderLine> {
        @Id
        @Column("ORDER_ID")
        private Long orderId;
        private List<ReflectiveSubItemOrderLine> items;
    }

    @Table("RF_SUBITEM_ORDER_LINE")
    private static class ReflectiveSubItemOrderLine {
        @Id
        @Column("LINE_ID")
        private Long lineId;
        @Column("PARENT_ORDER_ID")
        private ReflectiveSubItemOrder parent;
    }

    @Table("RF_COLUMN_METADATA")
    private static class ReflectiveColumnMetadataRecord {
        @Column(
                value = "RECORD_CODE",
                key = true,
                keyGroupName = "CODE",
                generationType = GenerationType.ON_INSERT,
                format = "yyyyMMdd",
                encryptType = EncryptType.MD5)
        private String code;
        @Column(value = "SECRET_VALUE", encryptType = EncryptType.RADOM_DECS)
        private String secret;
        @Column(value = "STATUS", defaultValue = "NEW")
        private String status;
        @Column(value = "CREATED_AT", generationExpression = "CURRENT_TIMESTAMP")
        private java.time.LocalDateTime createdAt;
    }

    @Table("RF_CUSTOMER_SNAPSHOT")
    private static class ReflectiveCustomerSnapshot {
        @Id
        @Column("CUSTOMER_ID")
        private Long customerId;
        @Column("DISPLAY_NAME")
        private String displayName;
    }

    @Table("RF_MULTI_ORDER")
    private static class ReflectiveMultiMapOrder {
        @Id
        @Column("ORDER_ID")
        private Long orderId;
        @Column(noMap = true)
        private String transientNote;
        @Column(value = "CUSTOMER_ID", propertyName = "customerId")
        @Column(value = "CUSTOMER_NAME", propertyName = "displayName")
        private ReflectiveCustomerSnapshot customer;
    }

    @Table(value = "RF_PREFIXED_ORDER", columnPrefix = "TENANT_LONG_")
    private static class ReflectivePrefixedOrder {
        @Id
        @Column(value = "ORDER_ID", preIndex = 0, preLen = 7)
        private Long orderId;
        @Column(value = "ORDER_STATUS", preIndex = 0, overrideParent = true)
        private String status;
        @Column(value = "CUSTOMER_ID", propertyName = "customerId", preIndex = 0)
        @Column(value = "CUSTOMER_NAME", propertyName = "displayName", preIndex = 0, overrideParent = true)
        private ReflectiveCustomerSnapshot customer;
    }

    private static final class RecordingDaoService extends DaoService {
        private final List<Object> created = new ArrayList<>();
        private final List<String> executedSql = new ArrayList<>();
        private long nextAuthorizedUserId = 1000;
        private long nextMenuId = 2000;
        private long nextRoleId = 3000;
        private long nextViewId = 4000;
        private long nextViewItemId = 4500;
        private long nextModelId = 5000;
        private long nextPropertyId = 6000;

        @Override
        public <T> void create(T object) {
            if (object instanceof AuthorizedUser user && user.getAuthorizedId() == null) {
                user.setAuthorizedId(nextAuthorizedUserId++);
            } else if (object instanceof AuthMenuItem item && item.getMenuId() == null) {
                item.setMenuId(nextMenuId++);
            } else if (object instanceof AuthRole role && role.getRoleId() == null) {
                role.setRoleId(nextRoleId++);
            } else if (object instanceof AppSystemView view && view.getViewId() == null) {
                view.setViewId(nextViewId++);
            } else if (object instanceof View view && view.getId() == null) {
                view.setId(nextViewId++);
            } else if (object instanceof ViewItem item && item.getId() == null) {
                item.setId(nextViewItemId++);
            } else if (object instanceof AppInstalledModel model && model.getModelId() == null) {
                model.setModelId(nextModelId++);
            } else if (object instanceof AppInstalledProperty property && property.getPropertyId() == null) {
                property.setPropertyId(nextPropertyId++);
            }
            created.add(object);
        }

        @Override
        public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {
            return List.of();
        }

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
        }
    }

    private static final class RecordingAppInstallGateway implements AppInstallGateway {
        private final List<String> actions = new ArrayList<>();
        private final List<BootstrapMenuItem> createdMenus = new ArrayList<>();
        private final List<BootstrapRole> createdRoles = new ArrayList<>();
        private final List<List<Model>> installedModelSchemas = new ArrayList<>();
        private final List<List<Model>> installedDefaultViews = new ArrayList<>();
        private final List<AppModuleSource> installedModuleSources = new ArrayList<>();
        private ApplicationDefinition createdApplication;
        private long nextViewId = 100;

        @Override
        public ApplicationDefinition createApplication(ApplicationDefinition app) {
            actions.add("createApplication:" + app.getAppId());
            return createdApplication;
        }

        @Override
        public void installApplicationModules(String sysCon) {
            actions.add("installApplicationModules:" + sysCon);
        }

        @Override
        public void installAuthorizationModules(String sysCon) {
            actions.add("installAuthorizationModules:" + sysCon);
        }

        @Override
        public void createAuthorizedUser(String sysCon, String userId) {
            actions.add("createAuthorizedUser:" + sysCon + ":" + userId);
        }

        @Override
        public void installUserModules(String sysCon, String databaseConnection) {
            actions.add("installUserModules:" + sysCon + ":" + databaseConnection);
        }

        @Override
        public List<String> installModuleSource(String sysCon, String databaseConnection, AppModuleSource source) {
            actions.add("installModuleSource:" + sysCon + ":" + databaseConnection + ":" + source.getModules().size());
            installedModuleSources.add(source);
            return List.of();
        }

        @Override
        public List<String> installModelSchemas(String sysCon, String databaseConnection, List<Model> models) {
            actions.add("installModelSchemas:" + sysCon + ":" + databaseConnection + ":" + models.size());
            installedModelSchemas.add(models);
            return List.of();
        }

        @Override
        public List<String> installDefaultViews(String sysCon, String databaseConnection, List<Model> models) {
            actions.add("installDefaultViews:" + sysCon + ":" + databaseConnection + ":" + models.size());
            installedDefaultViews.add(models);
            return List.of();
        }

        @Override
        public Long prepareAppSystemView(String sysCon, String viewName) {
            actions.add("prepareAppSystemView:" + sysCon + ":" + viewName);
            return nextViewId++;
        }

        @Override
        public void createMenu(String sysCon, BootstrapMenuItem menu) {
            actions.add("createMenu:" + sysCon + ":" + menu.getText());
            createdMenus.add(menu);
        }

        @Override
        public void createRole(String sysCon, BootstrapRole role) {
            actions.add("createRole:" + sysCon + ":" + role.getRoleName()
                    + ":" + role.getAuthorizedUserId() + ":" + role.getItems().size());
            createdRoles.add(role);
        }
    }

    private static EnumValue enumValue(String name, String value) {
        EnumValue enumValue = new EnumValue();
        enumValue.setName(name);
        enumValue.setValue(value);
        return enumValue;
    }
}
