package org.fool.framework.app;

import org.fool.framework.app.autoconfigure.AppInitializationProperties;
import org.fool.framework.app.autoconfigure.AppInitializationRunner;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.sqlscript.LegacyMysqlDdlGenerator;
import org.junit.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppInstallerInitializationTest {
    @Test
    public void initializesMetadataSchemaAndViewsInFixedOrder() {
        RecordingGateway gateway = new RecordingGateway();
        AppInstaller installer = new AppInstaller(gateway, AppBootstrapPlan.legacyDefaults());
        Model model = model("SystemModel", "example.SystemModel", "SW_SYSTEM_MODEL");
        AppModuleDefinition module = AppModuleDefinition.legacy(
                "SYS01",
                "System module",
                "1.0.0",
                List.of(model));

        SystemInitializationResult result = installer.initializeSystem(
                "sys-con",
                "data-con",
                new StaticAppModuleSource(List.of(module)));

        assertEquals(List.of("metadata", "schema", "views"), gateway.actions);
        assertEquals(1, result.discoveredModelCount());
        assertEquals(List.of("SYS01", "SystemModel"), result.installedMetadataItems());
        assertEquals(List.of("CREATE TABLE IF NOT EXISTS SW_SYSTEM_MODEL"), result.schemaStatements());
        assertEquals(List.of("SystemModel列表", "SystemModel明细"), result.defaultViews());
    }

    @Test
    public void startupRunnerDiscoversConfiguredPackagesAndUsesPrimaryDataSourceForBlankRoutes() {
        RecordingGateway gateway = new RecordingGateway();
        AppInstaller installer = new AppInstaller(gateway, AppBootstrapPlan.legacyDefaults());
        AppInitializationProperties properties = new AppInitializationProperties();
        properties.setModuleName("PKG01");
        properties.setRootPackage("org.fool.framework.app.reflective");
        properties.setDependencyPackages(List.of());
        properties.setMetadataConnection(" ");
        properties.setDataConnection(null);

        new AppInitializationRunner(installer, properties)
                .run(new DefaultApplicationArguments(new String[0]));

        assertEquals(List.of("metadata", "schema", "views"), gateway.actions);
        assertEquals(null, gateway.metadataConnection);
        assertEquals(null, gateway.dataConnection);
        assertTrue(gateway.models.stream().anyMatch(model -> "PackageOrder".equals(model.getName())));
        assertTrue(gateway.models.stream().anyMatch(model -> "PackageOrderState".equals(model.getName())));
    }

    @Test
    public void libraryStartupInitializationIsOptIn() {
        assertFalse(new AppInitializationProperties().isEnabled());
    }

    @Test
    public void compositeIdsKeepTheirDeclaredTypesAndDoNotGenerateMultipleAutoColumns() {
        ReflectiveAppModuleSource source = new ReflectiveAppModuleSource(
                "COMPOSITE",
                "Composite module",
                "1.0.0",
                List.of(CompositeInstallRecord.class));
        Model model = source.getModels().get(0);

        String ddl = new LegacyMysqlDdlGenerator().generateCreateTableSql(model);

        assertFalse(Boolean.TRUE.equals(model.getAutoSysId()));
        assertEquals(PropertyType.Long, model.getProperties().get(0).getPropertyType());
        assertEquals(PropertyType.String, model.getProperties().get(1).getPropertyType());
        assertFalse(ddl.contains("AUTO_INCREMENT"));
        assertTrue(ddl.contains("`APP_ID` BIGINT NOT NULL"));
        assertTrue(ddl.contains("`DB_NO` VARCHAR(200) NOT NULL"));
        assertTrue(ddl.contains("UNIQUE KEY"));
    }

    private static Model model(String name, String className, String tableName) {
        Model model = new Model();
        model.setName(name);
        model.setClassName(className);
        model.setTableName(tableName);
        model.setModelType(ModelType.DYNAMIC);
        model.setProperties(List.of());
        return model;
    }

    private static final class RecordingGateway implements AppInstallGateway {
        private final List<String> actions = new ArrayList<>();
        private List<Model> models = List.of();
        private String metadataConnection;
        private String dataConnection;

        @Override
        public List<String> installModuleSource(
                String metadataConnection,
                String dataConnection,
                AppModuleSource source) {
            actions.add("metadata");
            this.metadataConnection = metadataConnection;
            this.dataConnection = dataConnection;
            models = source.getModels();
            return List.of(source.getModules().get(source.getModules().size() - 1).getName(), models.get(0).getName());
        }

        @Override
        public List<String> installModelSchemas(
                String metadataConnection,
                String dataConnection,
                List<Model> models) {
            actions.add("schema");
            return List.of("CREATE TABLE IF NOT EXISTS " + models.get(0).getTableName());
        }

        @Override
        public List<String> installDefaultViews(
                String metadataConnection,
                String dataConnection,
                List<Model> models) {
            actions.add("views");
            return List.of(models.get(0).getName() + "列表", models.get(0).getName() + "明细");
        }

        @Override
        public ApplicationDefinition createApplication(ApplicationDefinition app) {
            return app;
        }

        @Override
        public void installApplicationModules(String sysCon) {
        }

        @Override
        public void installAuthorizationModules(String sysCon) {
        }

        @Override
        public void createAuthorizedUser(String sysCon, String userId) {
        }

        @Override
        public void installUserModules(String sysCon, String databaseConnection) {
        }

        @Override
        public Long prepareAppSystemView(String sysCon, String viewName) {
            return null;
        }

        @Override
        public void createMenu(String sysCon, BootstrapMenuItem menu) {
        }

        @Override
        public void createRole(String sysCon, BootstrapRole role) {
        }
    }

    @Table("COMPOSITE_INSTALL")
    private static final class CompositeInstallRecord {
        @Id("install")
        @Column("APP_ID")
        private Long appId;
        @Id("install")
        @Column("DB_NO")
        private String databaseNumber;
    }
}
