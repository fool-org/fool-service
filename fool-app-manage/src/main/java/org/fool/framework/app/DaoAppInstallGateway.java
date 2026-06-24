package org.fool.framework.app;

import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.sqlscript.LegacyMysqlDdlGenerator;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.service.LegacyAutoViewFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class DaoAppInstallGateway implements AppInstallGateway {
    private final DaoService defaultDaoService;
    private final Function<String, DaoService> daoRouter;
    private final LegacyMysqlDdlGenerator ddlGenerator;
    private final LegacyAutoViewFactory autoViewFactory;
    private final Map<String, AuthorizedUser> authorizedUsersByUserId = new ConcurrentHashMap<>();

    @Autowired
    public DaoAppInstallGateway(DaoService daoService, AppDaoServiceFactory daoServiceFactory) {
        this(
                daoService,
                connection -> routedDao(daoService, daoServiceFactory, connection),
                new LegacyMysqlDdlGenerator(),
                new LegacyAutoViewFactory());
    }

    public DaoAppInstallGateway(DaoService daoService) {
        this(daoService, connection -> daoService, new LegacyMysqlDdlGenerator(), new LegacyAutoViewFactory());
    }

    DaoAppInstallGateway(Function<String, DaoService> daoRouter) {
        this(null, daoRouter, new LegacyMysqlDdlGenerator(), new LegacyAutoViewFactory());
    }

    DaoAppInstallGateway(DaoService daoService, LegacyMysqlDdlGenerator ddlGenerator) {
        this(daoService, connection -> daoService, ddlGenerator, new LegacyAutoViewFactory());
    }

    DaoAppInstallGateway(
            DaoService daoService,
            LegacyMysqlDdlGenerator ddlGenerator,
            LegacyAutoViewFactory autoViewFactory) {
        this(daoService, connection -> daoService, ddlGenerator, autoViewFactory);
    }

    private DaoAppInstallGateway(
            DaoService defaultDaoService,
            Function<String, DaoService> daoRouter,
            LegacyMysqlDdlGenerator ddlGenerator,
            LegacyAutoViewFactory autoViewFactory) {
        this.defaultDaoService = defaultDaoService;
        this.daoRouter = daoRouter;
        this.ddlGenerator = ddlGenerator;
        this.autoViewFactory = autoViewFactory;
    }

    @Override
    public ApplicationDefinition createApplication(ApplicationDefinition app) {
        defaultDao().create(app);
        return app;
    }

    @Override
    public void installApplicationModules(String sysCon) {
        installRootModule(
                "SCPB07",
                "Soway.Model.App.Application",
                "1.0.1605.2401",
                sysCon,
                sysCon,
                "Application",
                "SW_APPLICATION",
                AppInstalledModel.CONNECTION_TYPE_APP_SYS);
    }

    @Override
    public void installAuthorizationModules(String sysCon) {
        installRootModule(
                "SWUA02",
                "SOWAY.ORM.AUTH.AuthorizedUser",
                "1.0.16045.3001",
                sysCon,
                sysCon,
                "AuthorizedUser",
                "SW_APP_AUTH_USER",
                AppInstalledModel.CONNECTION_TYPE_APP_SYS);
    }

    @Override
    public void createAuthorizedUser(String sysCon, String userId) {
        AuthorizedUser user = AuthorizedUser.forUser(userId);
        daoFor(sysCon).create(user);
        authorizedUsersByUserId.put(userId, user);
    }

    @Override
    public void installUserModules(String sysCon, String databaseConnection) {
        installRootModule(
                "SWUA01",
                "SOWAY.ORM.AUTH.User",
                "1.0.16015.3001",
                sysCon,
                databaseConnection,
                "User",
                "SW_AUTH_USER",
                AppInstalledModel.CONNECTION_TYPE_CURRENT);
    }

    @Override
    public List<String> installModuleSource(String sysCon, String databaseConnection, AppModuleSource source) {
        if (source == null || source.getModules().isEmpty()) {
            return List.of();
        }

        List<AppModuleDefinition> modules = source.getModules();
        List<String> installed = new ArrayList<>();
        Integer connectionType = connectionType(sysCon, databaseConnection);
        DaoService metadataDao = daoFor(sysCon);
        Map<Model, AppInstalledModel> installedModels = new IdentityHashMap<>();
        Map<Property, AppInstalledProperty> installedProperties = new IdentityHashMap<>();
        for (AppModuleDefinition module : modules) {
            if (module == null || module.getName() == null) {
                continue;
            }
            if (installModule(metadataDao, module, databaseConnection)) {
                installed.add(module.getName());
            }
            for (Model model : source.getModels(module)) {
                InstalledModelResult result = installModel(
                        metadataDao,
                        model,
                        module.getName(),
                        connectionType,
                        databaseConnection);
                if (result.model != null) {
                    installedModels.put(model, result.model);
                    if (model.getModelType() == ModelType.ENUM) {
                        installEnumValues(metadataDao, model, result.model);
                    }
                }
                if (result.created) {
                    installed.add(model.getName());
                }
            }
        }
        for (AppModuleDefinition module : modules) {
            if (module == null || module.getName() == null) {
                continue;
            }
            for (Model model : source.getModels(module)) {
                installProperties(
                        metadataDao,
                        model,
                        installedModels.get(model),
                        installedModels,
                        installedProperties,
                        connectionType,
                        databaseConnection);
            }
        }
        Set<Relation> installedRelations = Collections.newSetFromMap(new IdentityHashMap<>());
        for (AppModuleDefinition module : modules) {
            if (module == null || module.getName() == null) {
                continue;
            }
            for (Model model : source.getModels(module)) {
                installRelations(metadataDao, model, installedProperties, installedRelations);
            }
        }
        return installed;
    }

    public List<String> installModelSchemas(List<Model> models) {
        return installModelSchemas(defaultDao(), models);
    }

    private List<String> installModelSchemas(DaoService schemaDao, List<Model> models) {
        if (models == null || models.isEmpty()) {
            return List.of();
        }

        List<String> statements = new ArrayList<>();
        Set<Relation> seenRelations = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Model model : models) {
            if (model == null || model.getModelType() == ModelType.ENUM) {
                continue;
            }

            addStatement(statements, ddlGenerator.generateCreateTableSql(model));
            for (Relation relation : safeRelations(model)) {
                if (seenRelations.add(relation)) {
                    addStatement(statements, ddlGenerator.generateRelationSql(relation, model));
                }
            }
        }

        statements.forEach(schemaDao::execute);
        return statements;
    }

    @Override
    public List<String> installModelSchemas(String sysCon, String databaseConnection, List<Model> models) {
        return installModelSchemas(daoFor(databaseConnection), models);
    }

    @Override
    public List<String> installDefaultViews(String sysCon, String databaseConnection, List<Model> models) {
        if (models == null || models.isEmpty()) {
            return List.of();
        }

        List<String> viewNames = new ArrayList<>();
        DaoService metadataDao = daoFor(sysCon);
        for (Model model : models) {
            if (model == null || model.getModelType() == ModelType.ENUM) {
                continue;
            }
            addPersistedViewName(viewNames, persistView(metadataDao, autoViewFactory.createDefaultItemView(model)));
            addPersistedViewName(viewNames, persistView(metadataDao, autoViewFactory.createDefaultListView(model)));
        }
        return viewNames;
    }

    @Override
    public Long prepareAppSystemView(String sysCon, String viewName) {
        DaoService daoService = daoFor(sysCon);
        List<AppSystemView> views = daoService.selectList(
                AppSystemView.class,
                "SELECT `VIEW_ID`,`VIEW_NAME`,`VIEW_CONTYPE` FROM `SW_SYS_VIEW` WHERE `VIEW_NAME` = ?",
                viewName);
        if (!views.isEmpty()) {
            AppSystemView view = views.get(0);
            view.setConnectionType(AppSystemView.CONNECTION_TYPE_APP_SYS);
            daoService.save(view);
            return view.getViewId();
        }

        AppSystemView view = AppSystemView.appSys(viewName);
        daoService.create(view);
        return view.getViewId();
    }

    @Override
    public void createMenu(String sysCon, BootstrapMenuItem menu) {
        createMenuItem(daoFor(sysCon), menu);
    }

    @Override
    public void createRole(String sysCon, BootstrapRole role) {
        DaoService daoService = daoFor(sysCon);
        AuthRole authRole = AuthRole.fromBootstrap(role);
        daoService.create(authRole);

        AuthorizedUser authorizedUser = resolveAuthorizedUser(daoService, role.getAuthorizedUserId());
        daoService.create(AuthRoleAuthorizedUserRelation.of(authRole.getRoleId(), authorizedUser.getAuthorizedId()));

        for (BootstrapMenuItem item : role.getItems()) {
            if (item.getPersistedId() == null) {
                throw new IllegalStateException("Bootstrap menu item must be persisted before role relation: " + item.getText());
            }
            daoService.create(AuthRoleMenuItemRelation.of(authRole.getRoleId(), item.getPersistedId()));
        }
    }

    private void createMenuItem(DaoService daoService, BootstrapMenuItem menu) {
        AuthMenuItem authMenuItem = AuthMenuItem.fromBootstrap(menu);
        daoService.create(authMenuItem);
        menu.setPersistedId(authMenuItem.getMenuId());
        for (BootstrapMenuItem subItem : menu.getSubItems()) {
            createMenuItem(daoService, subItem);
            daoService.create(AuthMenuSubItemRelation.of(authMenuItem.getMenuId(), subItem.getPersistedId()));
        }
    }

    private void installRootModule(
            String moduleName,
            String sourceType,
            String version,
            String metadataConnection,
            String connection,
            String modelName,
            String tableName,
            Integer connectionType) {
        DaoService daoService = daoFor(metadataConnection);
        List<AppInstalledModule> modules = daoService.selectList(
                AppInstalledModule.class,
                "SELECT `MODULE_NAME`,`MODULE_REMARK`,`MODULE_ASSEMBLY`,`MODULE_FILENAME`,"
                        + "`MODULE_VERSION`,`MODULE_GENERATIONCODE`,`MODULE_CON` "
                        + "FROM `SW_SYS_MODULE` WHERE `MODULE_NAME` = ?",
                moduleName);
        if (!modules.isEmpty()) {
            installRootModel(metadataConnection, modelName, sourceType, tableName, moduleName, connectionType, connection);
            return;
        }

        daoService.create(AppInstalledModule.legacyRootModule(moduleName, sourceType, version, connection));
        installRootModel(metadataConnection, modelName, sourceType, tableName, moduleName, connectionType, connection);
    }

    private void installRootModel(
            String metadataConnection,
            String modelName,
            String className,
            String tableName,
            String moduleName,
            Integer connectionType,
            String connection) {
        DaoService daoService = daoFor(metadataConnection);
        List<AppInstalledModel> models = daoService.selectList(
                AppInstalledModel.class,
                "SELECT `MODEL_ID`,`MODEL_NAME`,`MODEL_CLASS`,`MODEL_CONTYPE`,`MODEL_DATABASETABLE`,"
                        + "`MODEL_MODULE`,`MODEL_AUTOID`,`MODEL_CON` "
                        + "FROM `SW_SYS_MODEL` WHERE `MODEL_CLASS` = ?",
                className);
        if (!models.isEmpty()) {
            return;
        }

        daoService.create(AppInstalledModel.legacyRootModel(
                modelName,
                className,
                tableName,
                moduleName,
                connectionType,
                connection));
    }

    private boolean installModule(DaoService daoService, AppModuleDefinition module, String connection) {
        List<AppInstalledModule> modules = daoService.selectList(
                AppInstalledModule.class,
                "SELECT `MODULE_NAME`,`MODULE_REMARK`,`MODULE_ASSEMBLY`,`MODULE_FILENAME`,"
                        + "`MODULE_VERSION`,`MODULE_GENERATIONCODE`,`MODULE_CON` "
                        + "FROM `SW_SYS_MODULE` WHERE `MODULE_NAME` = ?",
                module.getName());
        if (!modules.isEmpty()) {
            return false;
        }

        daoService.create(AppInstalledModule.fromDefinition(module, connection));
        return true;
    }

    private InstalledModelResult installModel(
            DaoService daoService,
            Model model,
            String moduleName,
            Integer connectionType,
            String connection) {
        if (model == null || model.getClassName() == null) {
            return new InstalledModelResult(null, false);
        }
        List<AppInstalledModel> models = daoService.selectList(
                AppInstalledModel.class,
                "SELECT `MODEL_ID`,`MODEL_NAME`,`MODEL_CLASS`,`MODEL_CONTYPE`,`MODEL_DATABASETABLE`,"
                        + "`MODEL_MODULE`,`MODEL_AUTOID`,`MODEL_CON` "
                        + "FROM `SW_SYS_MODEL` WHERE `MODEL_CLASS` = ?",
                model.getClassName());
        if (!models.isEmpty()) {
            return new InstalledModelResult(models.get(0), false);
        }

        AppInstalledModel installedModel = AppInstalledModel.fromModel(model, moduleName, connectionType, connection);
        daoService.create(installedModel);
        return new InstalledModelResult(installedModel, true);
    }

    private void installProperties(
            DaoService daoService,
            Model model,
            AppInstalledModel installedModel,
            Map<Model, AppInstalledModel> installedModels,
            Map<Property, AppInstalledProperty> installedProperties,
            Integer connectionType,
            String connection) {
        if (model == null || installedModel == null || model.getProperties() == null) {
            return;
        }
        for (org.fool.framework.model.model.Property property : model.getProperties()) {
            if (property == null || property.getName() == null) {
                continue;
            }
            AppInstalledProperty existing = findInstalledProperty(
                    daoService,
                    installedModel.getModelId(),
                    property.getName());
            if (existing != null) {
                installedProperties.put(property, existing);
                continue;
            }
            AppInstalledModel propertyModel = property.getPropertyModel() == null
                    ? null
                    : installedModels.get(property.getPropertyModel());
            Long propertyModelId = propertyModel == null ? null : propertyModel.getModelId();
            AppInstalledProperty installedProperty = AppInstalledProperty.fromProperty(
                    property,
                    installedModel,
                    propertyModelId,
                    connectionType,
                    connection);
            daoService.create(installedProperty);
            installedProperties.put(property, installedProperty);
        }
    }

    private void installEnumValues(
            DaoService daoService,
            Model model,
            AppInstalledModel installedModel) {
        if (model.getEnumValues() == null || model.getEnumValues().isEmpty()) {
            return;
        }
        for (EnumValue enumValue : model.getEnumValues()) {
            if (enumValue == null || isInstalledEnumValue(daoService, installedModel.getModelId(), enumValue)) {
                continue;
            }
            daoService.create(AppInstalledEnumValue.fromEnumValue(enumValue, installedModel.getModelId()));
        }
    }

    private boolean isInstalledEnumValue(DaoService daoService, Long ownerModelId, EnumValue enumValue) {
        List<AppInstalledEnumValue> values = daoService.selectList(
                AppInstalledEnumValue.class,
                "SELECT `EMUN_STR`,`EMUN_VALUE`,`SW_SYS_MODEL_EnumValuesMODEL_ID` "
                        + "FROM `SW_SYS_EMUNVALUE` WHERE `SW_SYS_MODEL_EnumValuesMODEL_ID` = ? "
                        + "AND `EMUN_STR` = ? AND `EMUN_VALUE` = ?",
                ownerModelId,
                enumValue.getName(),
                enumValue.getValue());
        return !values.isEmpty();
    }

    private AppInstalledProperty findInstalledProperty(DaoService daoService, Long ownerModelId, String propertyName) {
        List<AppInstalledProperty> properties = daoService.selectList(
                AppInstalledProperty.class,
                "SELECT `SysId`,`PROPERTY_TYPE`,`PROPERTY_CONTYPE`,`PROPERTY_NAME`,`PROPERTY_MODEL`,"
                        + "`PROPERTY_ISARRAY`,`PROPERTY_COLNAME`,`PROPERTY_PROPERTYNAME`,`PROPERTY_MULTIMAP`,"
                        + "`PROPERTY_IXGRPOUP`,`PROPERTY_ISCHECK`,`PROPERTY_GENERATIONTYPE`,"
                        + "`PROPERTY_ALLOWDBNULL`,`PROPERTY_CANGET`,`PROPERTY_CANSET`,`PROPERTY_FILTER`,"
                        + "`PROPERTY_SOURCE`,`PROPERTY_FORMAT`,`PROPERTY_SQLCON`,`SW_SYS_MODEL_PropertiesSysId` "
                        + "FROM `SW_SYS_PROPERTY` WHERE `SW_SYS_MODEL_PropertiesSysId` = ? AND `PROPERTY_PROPERTYNAME` = ?",
                ownerModelId,
                propertyName);
        return properties.isEmpty() ? null : properties.get(0);
    }

    private void installRelations(
            DaoService daoService,
            Model model,
            Map<Property, AppInstalledProperty> installedProperties,
            Set<Relation> installedRelations) {
        if (model == null || model.getRelations() == null) {
            return;
        }
        for (Relation relation : model.getRelations()) {
            if (relation == null || !installedRelations.add(relation)) {
                continue;
            }
            Long sourcePropertyId = installedPropertyId(installedProperties, relation.getProperty());
            Long targetPropertyId = installedPropertyId(installedProperties, relation.getTargetProperty());
            if (isInstalledRelation(
                    daoService,
                    sourcePropertyId,
                    relation.getRelationTable(),
                    relation.getPropertyColumn(),
                    relation.getTargetColumn())) {
                continue;
            }
            daoService.create(AppInstalledRelation.fromRelation(relation, sourcePropertyId, targetPropertyId));
        }
    }

    private Long installedPropertyId(
            Map<Property, AppInstalledProperty> installedProperties,
            Property property) {
        if (property == null) {
            return null;
        }
        AppInstalledProperty installedProperty = installedProperties.get(property);
        return installedProperty == null ? null : installedProperty.getPropertyId();
    }

    private boolean isInstalledRelation(
            DaoService daoService,
            Long sourcePropertyId,
            String relationTable,
            String propertyColumn,
            String targetColumn) {
        List<AppInstalledRelation> relations = daoService.selectList(
                AppInstalledRelation.class,
                "SELECT `SW_SYS_RELATION_TYPE`,`SW_SYS_RELATION_SOURCEPROPERTY`,"
                        + "`SW_SYS_RELATION_TARGETPROPERTY`,`SW_SYS_RELATION_TABLE`,"
                        + "`SW_SYS_RELATION_SOURCECOL`,`SW_SYS_RELATION_TARGETCOL`,"
                        + "`SW_SYS_RELATION_CANBENULL` "
                        + "FROM `SW_SYS_RELATION` WHERE `SW_SYS_RELATION_SOURCEPROPERTY` = ? "
                        + "AND `SW_SYS_RELATION_TABLE` = ? AND `SW_SYS_RELATION_SOURCECOL` = ? "
                        + "AND `SW_SYS_RELATION_TARGETCOL` = ?",
                sourcePropertyId,
                relationTable,
                propertyColumn,
                targetColumn);
        return !relations.isEmpty();
    }

    private DaoService defaultDao() {
        return daoFor(null);
    }

    private DaoService daoFor(String connection) {
        DaoService routed = daoRouter == null ? null : daoRouter.apply(connection);
        if (routed != null) {
            return routed;
        }
        if (defaultDaoService != null) {
            return defaultDaoService;
        }
        throw new IllegalStateException("No DaoService route configured for connection: " + connection);
    }

    private Integer connectionType(String sysCon, String databaseConnection) {
        return sysCon != null && sysCon.equals(databaseConnection)
                ? AppInstalledModel.CONNECTION_TYPE_APP_SYS
                : AppInstalledModel.CONNECTION_TYPE_CURRENT;
    }

    private List<Relation> safeRelations(Model model) {
        return model.getRelations() == null ? List.of() : model.getRelations();
    }

    private void addStatement(List<String> statements, String sql) {
        if (sql != null && !sql.isBlank()) {
            statements.add(sql);
        }
    }

    private void addPersistedViewName(List<String> viewNames, View view) {
        if (view != null && view.getViewName() != null) {
            viewNames.add(view.getViewName());
        }
    }

    private View persistView(DaoService daoService, View view) {
        if (view == null) {
            return null;
        }

        List<View> existingViews = daoService.selectList(
                View.class,
                "SELECT `id`,`view_name`,`view_text`,`view_remark`,`view_title`,`view_type`,"
                        + "`view_model`,`filter`,`view_model_class` FROM `fool_sys_view` WHERE `view_name` = ?",
                view.getViewName());
        if (!existingViews.isEmpty()) {
            return existingViews.get(0);
        }

        daoService.create(view);
        for (ViewItem item : safeViewItems(view)) {
            item.setViewId(view.getId());
            daoService.create(item);
        }
        return view;
    }

    private List<ViewItem> safeViewItems(View view) {
        return view.getListItems() == null ? List.of() : view.getListItems();
    }

    private AuthorizedUser resolveAuthorizedUser(DaoService daoService, String userId) {
        AuthorizedUser cached = authorizedUsersByUserId.get(userId);
        if (cached != null && cached.getAuthorizedId() != null) {
            return cached;
        }

        List<AuthorizedUser> users = daoService.selectList(
                AuthorizedUser.class,
                "SELECT `APP_AUTH_ID`,`APP_AUTH_USERID`,`APP_AUTH_USERLOGINNAME`,`APP_AUTH_DEP` "
                        + "FROM `SW_APP_AUTH_USER` WHERE `APP_AUTH_USERID` = ?",
                userId);
        if (!users.isEmpty()) {
            AuthorizedUser user = users.get(0);
            authorizedUsersByUserId.put(userId, user);
            return user;
        }

        AuthorizedUser user = AuthorizedUser.forUser(userId);
        daoService.create(user);
        authorizedUsersByUserId.put(userId, user);
        return user;
    }

    private static final class InstalledModelResult {
        private final AppInstalledModel model;
        private final boolean created;

        private InstalledModelResult(AppInstalledModel model, boolean created) {
            this.model = model;
            this.created = created;
        }
    }

    private static DaoService routedDao(
            DaoService defaultDaoService,
            AppDaoServiceFactory daoServiceFactory,
            String connection) {
        if (connection == null || connection.isBlank()) {
            return defaultDaoService;
        }
        DaoService routed = daoServiceFactory.create(connection);
        return routed == null ? defaultDaoService : routed;
    }
}
