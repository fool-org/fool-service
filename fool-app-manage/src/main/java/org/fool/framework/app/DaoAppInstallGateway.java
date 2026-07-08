package org.fool.framework.app;

import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.sqlscript.LegacyMysqlDdlGenerator;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.service.LegacyAutoViewFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                updateDefaultOwner(metadataDao, model, installedModels);
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
        for (AppModuleDefinition module : modules) {
            if (module == null || module.getName() == null) {
                continue;
            }
            for (Model model : source.getModels(module)) {
                installOperations(metadataDao, model, installedModels.get(model));
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
            addPersistedViewName(viewNames, persistView(metadataDao, autoViewFactory.createDefaultItemView(model), model.getId()));
            addPersistedViewName(viewNames, persistView(metadataDao, autoViewFactory.createDefaultListView(model), model.getId()));
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
                        + "`MODEL_MODULE`,`MODEL_AUTOID`,`MODEL_CON`,`MODEL_DEFAULTOWNER` "
                        + "FROM `SW_SYS_MODEL` WHERE `MODEL_CLASS` = ?",
                model.getClassName());
        if (!models.isEmpty()) {
            model.setId(models.get(0).getModelId());
            return new InstalledModelResult(models.get(0), false);
        }

        AppInstalledModel installedModel = AppInstalledModel.fromModel(model, moduleName, connectionType, connection);
        daoService.create(installedModel);
        model.setId(installedModel.getModelId());
        return new InstalledModelResult(installedModel, true);
    }

    private void updateDefaultOwner(
            DaoService daoService,
            Model model,
            Map<Model, AppInstalledModel> installedModels) {
        if (model == null || model.getOwner() == null) {
            return;
        }
        AppInstalledModel installedModel = installedModels.get(model);
        AppInstalledModel ownerModel = installedModels.get(model.getOwner());
        if (installedModel == null || ownerModel == null || ownerModel.getModelId() == null
                || Objects.equals(installedModel.getDefaultOwnerId(), ownerModel.getModelId())) {
            return;
        }
        installedModel.setDefaultOwnerId(ownerModel.getModelId());
        daoService.save(installedModel);
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
                property.setId(existing.getPropertyId());
                installedProperties.put(property, existing);
                installMultiDbMaps(daoService, property, existing);
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
            property.setId(installedProperty.getPropertyId());
            installedProperties.put(property, installedProperty);
            installMultiDbMaps(daoService, property, installedProperty);
        }
    }

    private void installMultiDbMaps(
            DaoService daoService,
            Property property,
            AppInstalledProperty installedProperty) {
        if (!Boolean.TRUE.equals(property.getMultiMap())
                || property.getDbMaps() == null
                || installedProperty.getPropertyId() == null) {
            return;
        }
        for (MultiDbMap dbMap : property.getDbMaps()) {
            if (dbMap == null || isInstalledMultiDbMap(daoService, installedProperty.getPropertyId(), dbMap)) {
                continue;
            }
            daoService.create(AppInstalledMultiDbMap.fromDbMap(dbMap, installedProperty.getPropertyId()));
        }
    }

    private void installOperations(DaoService daoService, Model model, AppInstalledModel installedModel) {
        if (model == null || installedModel == null || model.getOperations() == null) {
            return;
        }
        for (Operation operation : model.getOperations()) {
            if (operation == null || operation.getName() == null) {
                continue;
            }
            AppInstalledOperation existing = findInstalledOperation(
                    daoService,
                    installedModel.getModelId(),
                    operation.getName());
            if (existing != null) {
                operation.setId(existing.getOperationId());
                continue;
            }
            AppInstalledOperation installed = installedOperation(operation, installedModel.getModelId());
            daoService.create(installed);
            operation.setId(installed.getOperationId());
            for (OperationCommand command : safeOperationCommands(operation)) {
                AppInstalledOperationCommand installedCommand =
                        installedOperationCommand(command, installed.getOperationId());
                daoService.create(installedCommand);
                command.setId(installedCommand.getCommandId());
                command.setOwnerOperationId(installed.getOperationId());
            }
        }
    }

    private AppInstalledOperation findInstalledOperation(DaoService daoService, Long ownerModelId, String name) {
        List<AppInstalledOperation> operations = daoService.selectList(
                AppInstalledOperation.class,
                "SELECT `SysId`,`SW_SYS_MODEL_OperationsMODEL_ID`,`SW_MODEL_OPERATION_NAME`,"
                        + "`SW_MODEL_OPERATION_FILTER`,`SW_MODEL_OPERATION_BASETYPE`,"
                        + "`SW_MODEL_OPERATION_ARGMODEL`,`SW_MODEL_OPERATION_ARGFILTER`,"
                        + "`SW_MODEL_OPERATION_INVOKEDLL`,`SW_MODEL_OPERATION_INVOKECLASS`,"
                        + "`SW_MODEL_OPERATION_INVOKEMETHOD`,`SW_MODEL_OPERATION_RETURNMODEL` "
                        + "FROM `SW_SYS_OPERATION` WHERE `SW_SYS_MODEL_OperationsMODEL_ID` = ? "
                        + "AND `SW_MODEL_OPERATION_NAME` = ?",
                ownerModelId,
                name);
        return operations.isEmpty() ? null : operations.get(0);
    }

    private AppInstalledOperation installedOperation(Operation operation, Long ownerModelId) {
        AppInstalledOperation installed = new AppInstalledOperation();
        installed.setOwnerModelId(ownerModelId);
        installed.setName(operation.getName());
        installed.setFilter(operation.getFilter());
        installed.setBaseType(operation.getBaseOperationType() == null ? null : operation.getBaseOperationType().code());
        installed.setArgModelId(operation.getArgModelId());
        installed.setArgFilter(operation.getArgFilter());
        installed.setInvokeDll(operation.getInvokeDll());
        installed.setInvokeClass(operation.getInvokeClass());
        installed.setInvokeMethod(operation.getInvokeMethod());
        installed.setReturnModelId(operation.getReturnModelId());
        return installed;
    }

    private AppInstalledOperationCommand installedOperationCommand(OperationCommand command, Long ownerOperationId) {
        AppInstalledOperationCommand installed = new AppInstalledOperationCommand();
        installed.setOwnerOperationId(ownerOperationId);
        installed.setCommandType(command.getCommandType() == null ? null : command.getCommandType().code());
        installed.setPropertyId(command.getPropertyId());
        installed.setExpression(command.getExpression());
        installed.setArgModelId(command.getArgModelId());
        installed.setArgExpression(command.getArgExpression());
        installed.setArgSourceIdExpression(command.getArgSourceIdExpression());
        installed.setIndex(command.getIndex());
        installed.setPropertyExpression(command.getPropertyExpression());
        installed.setTempValue(command.getTempValue());
        return installed;
    }

    private List<OperationCommand> safeOperationCommands(Operation operation) {
        return operation.getCommands() == null ? List.of() : operation.getCommands();
    }

    private boolean isInstalledMultiDbMap(DaoService daoService, Long ownerPropertyId, MultiDbMap dbMap) {
        List<AppInstalledMultiDbMap> maps = daoService.selectList(
                AppInstalledMultiDbMap.class,
                "SELECT `SysId`,`MAP_NAME`,`MAP_COLNAME`,`SW_SYS_PROPERTY_DBMapsSysId` "
                        + "FROM `SW_SYS_MULTIMAP` WHERE `SW_SYS_PROPERTY_DBMapsSysId` = ? "
                        + "AND `MAP_NAME` = ? AND `MAP_COLNAME` = ?",
                ownerPropertyId,
                dbMap.getPropertyName(),
                dbMap.getColumnName());
        return !maps.isEmpty();
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

    private View persistView(DaoService daoService, View view, Long modelId) {
        if (view == null) {
            return null;
        }

        List<AppInstalledView> existingViews = daoService.selectList(
                AppInstalledView.class,
                "SELECT `VIEW_ID`,`VIEW_MODEL`,`VIEW_NAME`,`VIEW_FILTER`,`VIEW_DEFAULT`,"
                        + "`VIEW_TYPE`,`VIEW_CONTYPE`,`VIEW_FILE`,`VIEW_CHECKAUTH`,"
                        + "`VIEW_AUTOFRESHINTERVAL`,`VIEW_CANEDIT` "
                        + "FROM `SW_SYS_VIEW` WHERE `VIEW_NAME` = ?",
                view.getViewName());
        if (!existingViews.isEmpty()) {
            view.setId(existingViews.get(0).getViewId());
            return view;
        }

        AppInstalledView installedView = installedView(view, modelId);
        daoService.create(installedView);
        view.setId(installedView.getViewId());
        for (ViewItem item : safeViewItems(view)) {
            daoService.create(installedViewItem(item, view.getId()));
        }
        for (ViewOperation operation : safeViewOperations(view)) {
            AppInstalledOperationView operationView = installedOperationView(operation);
            if (operationView != null) {
                daoService.create(operationView);
            }
            daoService.create(installedViewOperation(
                    operation,
                    view.getId(),
                    operationView == null ? null : operationView.getOperationViewId()));
        }
        return view;
    }

    private AppInstalledView installedView(View view, Long modelId) {
        AppInstalledView installed = new AppInstalledView();
        installed.setModelId(modelId);
        installed.setName(view.getViewName());
        installed.setFilter(view.getFilter());
        installed.setDefaultViewId(view.getDefaultDetailView() == null ? null : view.getDefaultDetailView().getId());
        installed.setViewType(view.getViewType() == null ? null : view.getViewType().code());
        installed.setConnectionType(AppInstalledModel.CONNECTION_TYPE_CURRENT);
        installed.setAutoFreshInterval(view.getAutoFreshInterval());
        installed.setCanEdit(true);
        return installed;
    }

    private AppInstalledViewItem installedViewItem(ViewItem item, Long viewId) {
        AppInstalledViewItem installed = new AppInstalledViewItem();
        installed.setOwnerViewId(viewId);
        installed.setName(item.getItemName());
        installed.setFormat(item.getFormatRegx());
        installed.setPropertyId(item.getProperty() == null ? null : item.getProperty().getId());
        installed.setReadOnly(!item.isCanEdit());
        installed.setShowIndex(item.getShowIndex());
        installed.setListViewId(item.getListViewId());
        installed.setEditViewId(item.getEditViewId());
        installed.setSelectedViewId(item.getSelectedViewId());
        installed.setWidth(item.getWidth());
        installed.setShow(true);
        installed.setEditType(item.getEditType() == null ? null : item.getEditType().ordinal());
        installed.setSourceExpression(item.getSourceExpression());
        return installed;
    }

    private AppInstalledOperationView installedOperationView(ViewOperation operation) {
        if (operation.getOperation() == null) {
            return null;
        }
        AppInstalledOperationView installed = new AppInstalledOperationView();
        installed.setName(operation.getName());
        installed.setResultViewId(operation.getResultView() == null ? null : operation.getResultView().getId());
        installed.setOperationId(operation.getOperation().getId());
        installed.setSuccessMsg(operation.getSuccessMsg());
        installed.setErrorMsg(operation.getErrorMsg());
        installed.setMsg(operation.getConfirmMsg());
        installed.setShow(true);
        installed.setConfirmMsg(operation.getConfirmMsg());
        return installed;
    }

    private AppInstalledViewOperation installedViewOperation(
            ViewOperation operation,
            Long viewId,
            Long operationViewId) {
        AppInstalledViewOperation installed = new AppInstalledViewOperation();
        installed.setOwnerViewId(viewId);
        installed.setName(operation.getName());
        installed.setOperationViewId(operationViewId);
        installed.setResultViewId(operation.getResultView() == null ? null : operation.getResultView().getId());
        installed.setShowProcess(false);
        installed.setLocation(operation.getLocation());
        installed.setRequireSelect(operation.isRequireSelect());
        return installed;
    }

    private List<ViewItem> safeViewItems(View view) {
        return view.getListItems() == null ? List.of() : view.getListItems();
    }

    private List<ViewOperation> safeViewOperations(View view) {
        return view.getOperations() == null ? List.of() : view.getOperations();
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
