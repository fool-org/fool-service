package org.fool.framework.app;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DaoAppInstallGatewayInitializationTest {
    @Test
    public void resumesWhenRelationColumnAlreadyExists() {
        DuplicateRelationColumnDaoService daoService = new DuplicateRelationColumnDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = model("Order", "SW_ORDER");
        Property orderId = property("id", "ORDER_ID", PropertyType.IdentifyId);
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId));

        Relation relation = new Relation();
        relation.setRelationType(RelationType.One2Many);
        relation.setRelationTable("SW_ORDER_LINE");
        relation.setTargetColumn("ORDER_ID");
        order.setRelations(List.of(relation));

        List<String> attempted = gateway.installModelSchemas("sys-con", "work-con", List.of(order));

        assertEquals(2, attempted.size());
        assertEquals(2, daoService.executedSql.size());
        assertTrue(daoService.executedSql.get(1).startsWith("ALTER TABLE"));
    }

    @Test
    public void repairsDriftedCodeOwnedPropertyMetadata() {
        DriftedPropertyDaoService daoService = new DriftedPropertyDaoService();
        DaoAppInstallGateway gateway = new DaoAppInstallGateway(daoService);
        Model order = model("Order", "SW_ORDER");
        order.setProperties(List.of(property("symbol", "ORDER_SYMBOL", PropertyType.String)));

        gateway.installModuleSource(
                "sys-con",
                "work-con",
                new StaticAppModuleSource(List.of(AppModuleDefinition.legacy(
                        "MKT01",
                        "example.OrderModule",
                        "2.0.0",
                        List.of(order)))));

        assertEquals(1, daoService.saved.size());
        AppInstalledProperty repaired = (AppInstalledProperty) daoService.saved.get(0);
        assertEquals(Long.valueOf(21L), repaired.getPropertyId());
        assertEquals(PropertyType.String, repaired.getPropertyType());
        assertEquals("ORDER_SYMBOL", repaired.getColumnName());
    }

    private static Model model(String name, String tableName) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName(tableName);
        model.setAutoSysId(false);
        return model;
    }

    private static Property property(String name, String column, PropertyType type) {
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

    private static final class DuplicateRelationColumnDaoService extends DaoService {
        private final List<String> executedSql = new ArrayList<>();

        @Override
        public void execute(String sql) {
            executedSql.add(sql);
            if (sql.startsWith("ALTER TABLE")) {
                throw new BadSqlGrammarException(
                        "relation column already exists",
                        sql,
                        new SQLException("Duplicate column name", "42S21", 1060));
            }
        }
    }

    private static final class DriftedPropertyDaoService extends DaoService {
        private final List<Object> saved = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {
            if (clazz == AppInstalledModule.class) {
                AppInstalledModule module = new AppInstalledModule();
                module.setModuleName("MKT01");
                return (List<T>) List.of(module);
            }
            if (clazz == AppInstalledModel.class) {
                AppInstalledModel model = AppInstalledModel.legacyRootModel(
                        "Order",
                        "example.Order",
                        "SW_ORDER",
                        "MKT01",
                        AppInstalledModel.CONNECTION_TYPE_CURRENT,
                        "work-con");
                model.setModelId(11L);
                return (List<T>) List.of(model);
            }
            if (clazz == AppInstalledProperty.class) {
                AppInstalledProperty property = new AppInstalledProperty();
                property.setPropertyId(21L);
                property.setPropertyType(PropertyType.IdentifyId);
                property.setConnectionType(AppInstalledModel.CONNECTION_TYPE_CURRENT);
                property.setName("symbol");
                property.setPropertyName("symbol");
                property.setColumnName("ORDER_SYMBOL");
                property.setArray(false);
                property.setMultiMap(false);
                property.setCheck(false);
                property.setAllowDbNull(false);
                property.setCanGet(true);
                property.setCanSet(true);
                property.setPropertySqlCon("work-con");
                property.setOwnerModelId(11L);
                return (List<T>) List.of(property);
            }
            return List.of();
        }

        @Override
        public <T> boolean save(T object) {
            saved.add(object);
            return true;
        }
    }
}
