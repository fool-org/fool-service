package org.fool.framework.model.sqlscript;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.SimpleFilter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SqlGeneratorTest {

    @Test
    public void generateSelectPlacesLegacyDefaultOrderBeforePagination() {
        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property symbol = property("symbol", "order_symbol");
        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageIndex(2);
        pageNavigator.setPageSize(10);

        QueryAndArgs query = new SqlGenerator().generateSelect(
                order,
                List.of(orderId, symbol),
                IQueryFilter.init(),
                pageNavigator,
                "order_id",
                true);

        assertEquals(
                "SELECT order_id,order_symbol FROM `market_order` WHERE 1=1  AND  1=1  ORDER BY `order_id` DESC LIMIT ? OFFSET ?",
                query.getSql());
        assertArrayEquals(new Object[]{10, 10}, query.getArgs());
    }

    @Test
    public void generateSelectJoinsLegacyBusinessObjectShowProperty() {
        Model customer = model("Customer", "customer");
        Property customerId = property("customerId", "customer_id");
        Property customerName = property("customerName", "customer_name");
        customer.setIdProperty(customerId);
        customer.setShowProperty(customerName);
        customer.setProperties(List.of(customerId, customerName));

        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);

        QueryAndArgs query = new SqlGenerator().generateSelect(
                order,
                List.of(orderId, customerProperty),
                IQueryFilter.init());

        assertEquals(
                "SELECT `market_order`.`order_id` AS `order_id`,"
                        + "`customer`.`customer_id` AS `customer_customer_id`,"
                        + "`customer`.`customer_name` AS `customer_customer_name`"
                        + " FROM `market_order` LEFT OUTER JOIN `customer` AS `customer`"
                        + " ON `customer`.`customer_id`=`market_order`.`customer_id`"
                        + " WHERE 1=1  AND  1=1 ",
                query.getSql());
        assertArrayEquals(new Object[]{}, query.getArgs());
    }

    @Test
    public void generateSelectOrdersByLegacyJoinedBusinessObjectShowProperty() {
        Model customer = model("Customer", "customer");
        Property customerId = property("customerId", "customer_id");
        Property customerName = property("customerName", "customer_name");
        customer.setIdProperty(customerId);
        customer.setShowProperty(customerName);
        customer.setProperties(List.of(customerId, customerName));

        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);

        QueryAndArgs query = new SqlGenerator().generateSelect(
                order,
                List.of(orderId, customerProperty),
                IQueryFilter.init(),
                null,
                "`customer`.`customer_name`",
                true);

        assertEquals(
                "SELECT `market_order`.`order_id` AS `order_id`,"
                        + "`customer`.`customer_id` AS `customer_customer_id`,"
                        + "`customer`.`customer_name` AS `customer_customer_name`"
                        + " FROM `market_order` LEFT OUTER JOIN `customer` AS `customer`"
                        + " ON `customer`.`customer_id`=`market_order`.`customer_id`"
                        + " WHERE 1=1  AND  1=1  ORDER BY `customer`.`customer_name` DESC",
                query.getSql());
        assertArrayEquals(new Object[]{}, query.getArgs());
    }

    @Test
    public void generateSelectAliasesLegacyMultiMapBusinessObjectColumns() {
        Model customer = model("Customer", "customer");
        Property customerId = property("customerId", "customer_id");
        Property customerName = property("customerName", "customer_name");
        customer.setProperties(List.of(customerId, customerName));

        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property customerSnapshot = property("customer", null);
        customerSnapshot.setPropertyType(PropertyType.BusinessObject);
        customerSnapshot.setPropertyModel(customer);
        customerSnapshot.setMultiMap(true);
        customerSnapshot.setDbMaps(List.of(
                new org.fool.framework.model.model.MultiDbMap("customerId", "customer_id"),
                new org.fool.framework.model.model.MultiDbMap("customerName", "customer_name")));

        QueryAndArgs query = new SqlGenerator().generateSelect(
                order,
                List.of(orderId, customerSnapshot),
                IQueryFilter.init());

        assertEquals(
                "SELECT order_id,`market_order`.`customer_id` AS `customer_customerId`,"
                        + "`market_order`.`customer_name` AS `customer_customerName`"
                        + " FROM `market_order` WHERE 1=1  AND  1=1 ",
                query.getSql());
        assertArrayEquals(new Object[]{}, query.getArgs());
    }

    @Test
    public void generateSelectCountJoinsLegacyBusinessObjectShowPropertyForFilters() {
        Model customer = model("Customer", "customer");
        Property customerId = property("customerId", "customer_id");
        Property customerName = property("customerName", "customer_name");
        customer.setIdProperty(customerId);
        customer.setShowProperty(customerName);
        customer.setProperties(List.of(customerId, customerName));

        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);

        QueryAndArgs query = new SqlGenerator().generateSelectCount(
                order,
                new SimpleFilter() {
                    @Override
                    public QueryAndArgs generateSql() {
                        QueryAndArgs queryAndArgs = new QueryAndArgs();
                        queryAndArgs.setSql("`customer`.`customer_name` LIKE ?");
                        queryAndArgs.setArgs(new Object[]{"%Ada%"});
                        return queryAndArgs;
                    }
                },
                List.of(orderId, customerProperty));

        assertEquals(
                "SELECT  COUNT(1) FROM `market_order` LEFT OUTER JOIN `customer` AS `customer`"
                        + " ON `customer`.`customer_id`=`market_order`.`customer_id`"
                        + " WHERE 1=1  AND `customer`.`customer_name` LIKE ?",
                query.getSql());
        assertArrayEquals(new Object[]{"%Ada%"}, query.getArgs());
    }

    @Test
    public void generateSelectSkipsLegacyMultiMapWhenDbMapsMissing() {
        Model order = model("Order", "market_order");
        Property orderId = property("orderId", "order_id");
        Property customerProperty = property("customer", null);
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setMultiMap(true);

        QueryAndArgs query = new SqlGenerator().generateSelect(
                order,
                List.of(orderId, customerProperty),
                IQueryFilter.init());

        assertEquals("SELECT order_id FROM `market_order` WHERE 1=1  AND  1=1 ", query.getSql());
        assertArrayEquals(new Object[]{}, query.getArgs());
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

    private static Property property(String name, String column) {
        Property property = new Property();
        property.setName(name);
        property.setRemark(name);
        property.setColumn(column);
        property.setPropertyType(PropertyType.String);
        property.setIsCollection(false);
        property.setAllowDbNull(false);
        property.setCheck(false);
        return property;
    }
}
