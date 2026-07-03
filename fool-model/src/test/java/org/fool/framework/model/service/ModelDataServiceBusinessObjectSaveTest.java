package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.Application;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class ModelDataServiceBusinessObjectSaveTest {

    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void saveDataStoresBusinessObjectIdInLegacyForeignKeyColumn() {
        String tableName = "runtime_save_business_order";
        cleanupTable(tableName);
        try {
            jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                    + "`ORDER_ID` varchar(64) NOT NULL,"
                    + "`CUSTOMER_ID` bigint DEFAULT NULL,"
                    + "PRIMARY KEY (`ORDER_ID`))");
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`CUSTOMER_ID`) VALUES (?,?)",
                    "6001",
                    1L);
            Model order = businessObjectOrderModel(tableName);
            DbMysqlDynamic customer = new DbMysqlDynamic(order.getProperties().get(1).getPropertyModel());
            customer.set("customerId", 42L);
            DbMysqlDynamic data = new DbMysqlDynamic(order);
            data.set("orderId", "6001");
            data.set("customer", customer);

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Long customerId = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_ID` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Long.class,
                    "6001");
            assertEquals(Long.valueOf(42L), customerId);
        } finally {
            cleanupTable(tableName);
        }
    }

    private Model businessObjectOrderModel(String tableName) {
        Model customer = new Model();
        Property customerId = columnProperty("customerId", "CUSTOMER_ID", PropertyType.Long);
        customer.setIdProperty(customerId);
        customer.setProperties(List.of(customerId));

        Model order = new Model();
        order.setTableName(tableName);
        Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
        order.setIdProperty(orderId);
        Property customerRef = columnProperty("customer", "CUSTOMER_ID", PropertyType.BusinessObject);
        customerRef.setPropertyModel(customer);
        order.setProperties(List.of(orderId, customerRef));
        return order;
    }

    private Property columnProperty(String name, String column, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        property.setPropertyType(type);
        property.setIsCollection(false);
        property.setMultiMap(false);
        return property;
    }

    private void cleanupTable(String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
    }
}
