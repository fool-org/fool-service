package org.fool.framework.model.sqlscript;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacyMysqlDdlGeneratorTest {
    @Test
    public void generatesCreateTableSqlUsingLegacyModelFactoryRules() throws Exception {
        Model order = model("Order", "SW_ORDER", false);
        Property id = property("id", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        Property name = property("name", "ORDER_NAME", PropertyType.String, false, false, null, false);
        Property status = property("status", "ORDER_STATUS", PropertyType.Int, false, true, "STATUS", false);
        Property externalNo = property("externalNo", "EXT_NO", PropertyType.String, false, true, "STATUS", false);
        Property note = property("note", "ORDER_NOTE", PropertyType.String, true, false, null, false);
        Property lines = property("lines", "LINE_ID", PropertyType.Long, false, false, null, true);
        order.setIdProperty(id);
        order.setProperties(List.of(id, name, status, externalNo, note, lines));

        String sql = generateCreateTableSql(order);

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `SW_ORDER`"));
        assertTrue(sql.contains("`ORDER_ID` BIGINT NOT NULL AUTO_INCREMENT"));
        assertTrue(sql.contains("`ORDER_NAME` VARCHAR(200) NOT NULL"));
        assertTrue(sql.contains("`ORDER_STATUS` INT NOT NULL"));
        assertTrue(sql.contains("`ORDER_NOTE` VARCHAR(200) NULL"));
        assertTrue(sql.contains("PRIMARY KEY (`ORDER_ID`)"));
        assertTrue(sql.contains("UNIQUE KEY `IX_STATUS_SW_ORDER` (`ORDER_STATUS`,`EXT_NO`)"));
        assertFalse(sql.contains("LINE_ID"));
    }

    @Test
    public void generatesSysIdPrimaryKeyWhenLegacyModelUsesAutoSysId() throws Exception {
        Model model = model("Audit", "SW_AUDIT", true);
        Property code = property("code", "AUDIT_CODE", PropertyType.String, false, false, null, false);
        model.setProperties(List.of(code));

        String sql = generateCreateTableSql(model);

        assertTrue(sql.contains("`SysId` BIGINT NOT NULL AUTO_INCREMENT"));
        assertTrue(sql.contains("PRIMARY KEY (`SysId`)"));
        assertFalse(sql.contains("PRIMARY KEY (`AUDIT_CODE`)"));
    }

    @Test
    public void generatesCreateTableSqlForLegacyMultiDbMapsAndNoMapColumns() throws Exception {
        Model customer = model("Customer", "SW_CUSTOMER", false);
        Property customerId = property("customerId", "CUSTOMER_ID", PropertyType.Long, false, true, "", false);
        Property displayName = property("displayName", "DISPLAY_NAME", PropertyType.String, false, false, null, false);
        customer.setIdProperty(customerId);
        customer.setProperties(List.of(customerId, displayName));

        Model order = model("Order", "SW_ORDER", false);
        Property orderId = property("orderId", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        Property transientNote = property("transientNote", null, PropertyType.String, true, false, null, false);
        Property customerSnapshot = property("customer", null, PropertyType.BusinessObject, false, false, null, false);
        customerSnapshot.setPropertyModel(customer);
        customerSnapshot.setMultiMap(true);
        customerSnapshot.setDbMaps(List.of(
                new MultiDbMap("customerId", "CUSTOMER_ID"),
                new MultiDbMap("displayName", "CUSTOMER_NAME")));
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId, transientNote, customerSnapshot));

        String sql = generateCreateTableSql(order);

        assertTrue(sql.contains("`CUSTOMER_ID` BIGINT NOT NULL"));
        assertTrue(sql.contains("`CUSTOMER_NAME` VARCHAR(200) NOT NULL"));
        assertFalse(sql.contains("transientNote"));
        assertFalse(sql.contains("`CUSTOMER`"));
    }

    @Test
    public void generateCreateTableUsesLegacyColumnDefaultValues() throws Exception {
        Model order = model("Order", "SW_ORDER", false);
        Property id = property("id", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        Property status = property("status", "ORDER_STATUS", PropertyType.String, false, false, null, false);
        Property retryCount = property("retryCount", "RETRY_COUNT", PropertyType.Int, false, false, null, false);
        status.setDefaultValue("READY's");
        retryCount.setDefaultValue("0");
        order.setIdProperty(id);
        order.setProperties(List.of(id, status, retryCount));

        String sql = generateCreateTableSql(order);

        assertTrue(sql.contains("`ORDER_STATUS` VARCHAR(200) NOT NULL DEFAULT 'READY''s'"));
        assertTrue(sql.contains("`RETRY_COUNT` INT NOT NULL DEFAULT '0'"));
        assertFalse(sql.contains("`ORDER_ID` BIGINT NOT NULL AUTO_INCREMENT DEFAULT"));
    }

    @Test
    public void generateCreateTableUsesLegacyColumnGenerationExpressions() throws Exception {
        Model order = model("Order", "SW_ORDER", false);
        Property id = property("id", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        Property createdAt = property("createdAt", "CREATED_AT", PropertyType.DateTime, false, false, null, false);
        createdAt.setGenerationExpression("CURRENT_TIMESTAMP");
        order.setIdProperty(id);
        order.setProperties(List.of(id, createdAt));

        String sql = generateCreateTableSql(order);

        assertTrue(sql.contains("`CREATED_AT` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP)"));
        assertFalse(sql.contains("`ORDER_ID` BIGINT NOT NULL AUTO_INCREMENT DEFAULT"));
    }

    @Test
    public void generatesOneToManyRelationAlterSqlUsingSourceModelKeyType() throws Exception {
        Model order = model("Order", "SW_ORDER", false);
        Property id = property("id", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        order.setIdProperty(id);
        order.setProperties(List.of(id));

        Relation relation = new Relation();
        relation.setRelationType(RelationType.One2Many);
        relation.setRelationTable("SW_ORDER_LINE");
        relation.setTargetColumn("ORDER_ID");

        String sql = new LegacyMysqlDdlGenerator().generateRelationSql(relation, order);

        assertTrue(sql.contains("ALTER TABLE `SW_ORDER_LINE`"));
        assertTrue(sql.contains("ADD COLUMN `ORDER_ID` BIGINT NULL"));
    }

    @Test
    public void generatesManyToManyRelationTableSqlUsingBothModelKeyTypes() throws Exception {
        Model order = model("Order", "SW_ORDER", false);
        Property orderId = property("id", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId));

        Model role = model("Role", "SW_ROLE", false);
        Property roleId = property("id", "ROLE_ID", PropertyType.Long, false, true, "", false);
        role.setIdProperty(roleId);
        role.setProperties(List.of(roleId));

        Property target = property("role", "ROLE_ID", PropertyType.BusinessObject, false, false, null, false);
        target.setPropertyModel(role);

        Relation relation = new Relation();
        relation.setRelationType(RelationType.Many2Many);
        relation.setRelationTable("SW_ORDER_ROLE");
        relation.setPropertyColumn("ORDER_ID");
        relation.setTargetColumn("ROLE_ID");
        relation.setTargetProperty(target);

        String sql = new LegacyMysqlDdlGenerator().generateRelationSql(relation, order);

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `SW_ORDER_ROLE`"));
        assertTrue(sql.contains("`ORDER_ID` BIGINT NOT NULL"));
        assertTrue(sql.contains("`ROLE_ID` BIGINT NOT NULL"));
    }

    @Test
    public void generatesRelationTableSqlUsingLongWhenTargetPropertyIsMissing() throws Exception {
        Model order = model("Order", "SW_ORDER", false);
        Property orderId = property("id", "ORDER_ID", PropertyType.IdentifyId, false, true, "", false);
        order.setIdProperty(orderId);
        order.setProperties(List.of(orderId));

        Relation relation = new Relation();
        relation.setRelationType(RelationType.Recurve);
        relation.setRelationTable("SW_ORDER_RELATED");
        relation.setPropertyColumn("ORDER_ID");
        relation.setTargetColumn("RELATED_ID");

        String sql = new LegacyMysqlDdlGenerator().generateRelationSql(relation, order);

        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS `SW_ORDER_RELATED`"));
        assertTrue(sql.contains("`ORDER_ID` BIGINT NOT NULL"));
        assertTrue(sql.contains("`RELATED_ID` BIGINT NOT NULL"));
    }

    private static String generateCreateTableSql(Model model) throws Exception {
        Class<?> generatorClass = Class.forName("org.fool.framework.model.sqlscript.LegacyMysqlDdlGenerator");
        Object generator = generatorClass.getDeclaredConstructor().newInstance();
        Method method = generatorClass.getMethod("generateCreateTableSql", Model.class);
        return (String) method.invoke(generator, model);
    }

    private static Model model(String name, String tableName, boolean autoSysId) throws Exception {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName(tableName);
        setField(model, "autoSysId", autoSysId);
        return model;
    }

    private static Property property(
            String name,
            String column,
            PropertyType type,
            boolean allowNull,
            boolean check,
            String ixGroup,
            boolean collection) throws Exception {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        property.setIsCollection(collection);
        setField(property, "propertyType", type);
        setField(property, "allowDbNull", allowNull);
        setField(property, "check", check);
        setField(property, "ixGroup", ixGroup);
        return property;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
