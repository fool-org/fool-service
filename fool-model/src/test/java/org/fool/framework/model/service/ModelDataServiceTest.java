package org.fool.framework.model.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.Application;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
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
import static org.junit.Assert.assertNotNull;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class ModelDataServiceTest {

    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getModel() {

        String modelName = "user";
        var model = modelDataService.getModel(modelName);
        log.info("the model is :{}", model);
    }


    @Test
    public void getDataListWithPageInfo(){

    }

    @Test
    public void getModelRehydratesRuntimeEnumValuesFromDetailRows() {
        long modelId = 91001L;
        String modelName = "RuntimeEnumState";
        cleanupRuntimeEnumModel(modelId, modelName);
        try {
            jdbcTemplate.update(
                    "INSERT INTO `fool_sys_model` "
                            + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                            + "VALUES (?,?,?,?,?,?,?,?,?)",
                    modelId,
                    modelName,
                    modelName,
                    "runtime enum rehydration test",
                    ModelType.ENUM.code(),
                    "example.RuntimeEnumState",
                    null,
                    false,
                    null);
            jdbcTemplate.update(
                    "INSERT INTO `fool_sys_model_enum` (`name`,`value`,`remark`,`owner`) VALUES (?,?,?,?)",
                    "OPEN",
                    "0",
                    "open state",
                    modelId);
            jdbcTemplate.update(
                    "INSERT INTO `fool_sys_model_enum` (`name`,`value`,`remark`,`owner`) VALUES (?,?,?,?)",
                    "CLOSED",
                    "1",
                    "closed state",
                    modelId);

            Model model = modelDataService.getModel(modelName);

            assertEquals(ModelType.ENUM, model.getModelType());
            assertEquals(
                    List.of("OPEN:0", "CLOSED:1"),
                    model.getEnumValues().stream()
                            .map(value -> value.getName() + ":" + value.getValue())
                            .toList());
        } finally {
            cleanupRuntimeEnumModel(modelId, modelName);
        }
    }

    @Test
    public void getOneDataLoadsLegacyDetailByModelIdAndDataId() {
        long modelId = 92001L;
        long idPropertyId = 92002L;
        long namePropertyId = 92003L;
        String modelName = "RuntimeDetailOrder";
        String tableName = "runtime_detail_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "1001",
                    "Legacy detail");

            IDynamicData data = modelDataService.getOneData(modelName, "1001");

            assertNotNull(data);
            assertEquals("1001", data.getId());
            assertEquals("Legacy detail", data.get("orderName"));
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void createDataInsertsLegacySimpleDynamicRow() {
        long modelId = 93001L;
        long idPropertyId = 93002L;
        long namePropertyId = 93003L;
        String modelName = "RuntimeCreateOrder";
        String tableName = "runtime_create_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "2001");
            data.set("orderName", "Created detail");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + tableName + "` WHERE `ORDER_ID` = ? AND `ORDER_NAME` = ?",
                    Integer.class,
                    "2001",
                    "Created detail");
            assertEquals(1, count.intValue());
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void deleteDataDeletesLegacySimpleDynamicRowById() {
        long modelId = 94001L;
        long idPropertyId = 94002L;
        long namePropertyId = 94003L;
        String modelName = "RuntimeDeleteOrder";
        String tableName = "runtime_delete_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "3001",
                    "Delete detail");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "3001");

            assertEquals(Boolean.TRUE, modelDataService.deleteData(data));

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Integer.class,
                    "3001");
            assertEquals(0, count.intValue());
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataUpdatesLegacySimpleDynamicRowById() {
        long modelId = 95001L;
        long idPropertyId = 95002L;
        long namePropertyId = 95003L;
        String modelName = "RuntimeSaveOrder";
        String tableName = "runtime_save_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "4001",
                    "Before save");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("orderId", "4001");
            data.set("orderName", "After save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "4001");
            assertEquals("After save", name);
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataListUpdatesLegacySimpleDynamicRows() {
        long modelId = 96001L;
        long idPropertyId = 96002L;
        long namePropertyId = 96003L;
        String modelName = "RuntimeBatchSaveOrder";
        String tableName = "runtime_batch_save_order";
        cleanupRuntimeDetailModel(modelId, modelName, tableName);
        try {
            createRuntimeDetailModel(modelId, idPropertyId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "5001",
                    "Before save 1");
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`ORDER_NAME`) VALUES (?,?)",
                    "5002",
                    "Before save 2");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic first = new DbMysqlDynamic(model);
            first.set("orderId", "5001");
            first.set("orderName", "After save 1");
            DbMysqlDynamic second = new DbMysqlDynamic(model);
            second.set("orderId", "5002");
            second.set("orderName", "After save 2");

            assertEquals(Boolean.TRUE, modelDataService.saveDataList(List.of(first, second)));

            List<String> names = jdbcTemplate.queryForList(
                    "SELECT `ORDER_NAME` FROM `" + tableName + "` ORDER BY `ORDER_ID`",
                    String.class);
            assertEquals(List.of("After save 1", "After save 2"), names);
        } finally {
            cleanupRuntimeDetailModel(modelId, modelName, tableName);
        }
    }

    @Test
    public void createDataWritesLegacyMultiDbMapColumns() {
        String tableName = "runtime_create_dbmaps_order";
        cleanupRuntimeDbMapsTable(tableName);
        try {
            createRuntimeDbMapsTable(tableName);
            Model order = dbMapsOrderModel(tableName);
            DbMysqlDynamic data = dbMapsOrderData(order, "7001", 42L, "Ada");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            Long customerId = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_ID` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Long.class,
                    "7001");
            String customerName = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "7001");
            assertEquals(Long.valueOf(42L), customerId);
            assertEquals("Ada", customerName);
        } finally {
            cleanupRuntimeDbMapsTable(tableName);
        }
    }

    @Test
    public void saveDataUpdatesLegacyMultiDbMapColumnsById() {
        String tableName = "runtime_save_dbmaps_order";
        cleanupRuntimeDbMapsTable(tableName);
        try {
            createRuntimeDbMapsTable(tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`ORDER_ID`,`CUSTOMER_ID`,`CUSTOMER_NAME`) VALUES (?,?,?)",
                    "8001",
                    1L,
                    "Before save");
            Model order = dbMapsOrderModel(tableName);
            DbMysqlDynamic data = dbMapsOrderData(order, "8001", 42L, "Ada");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            Long customerId = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_ID` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    Long.class,
                    "8001");
            String customerName = jdbcTemplate.queryForObject(
                    "SELECT `CUSTOMER_NAME` FROM `" + tableName + "` WHERE `ORDER_ID` = ?",
                    String.class,
                    "8001");
            assertEquals(Long.valueOf(42L), customerId);
            assertEquals("Ada", customerName);
        } finally {
            cleanupRuntimeDbMapsTable(tableName);
        }
    }

    @Test
    public void initDataBuildsLegacySimpleDynamicDefaults() {
        Model model = new Model();
        model.setProperties(List.of(
                simpleProperty("active", PropertyType.Boolean),
                simpleProperty("count", PropertyType.Int),
                simpleProperty("total", PropertyType.Long),
                simpleProperty("code", PropertyType.String)));

        IDynamicData data = modelDataService.initData(model);

        assertNotNull(data);
        assertEquals(false, data.get("active"));
        assertEquals(0, data.get("count"));
        assertEquals(0L, data.get("total"));
        assertEquals("", data.get("code"));
    }

    @Test
    public void initDataBuildsLegacyCollectionDefaults() {
        Model model = new Model();
        model.setProperties(List.of(collectionProperty("items")));

        IDynamicData data = modelDataService.initData(model);

        assertNotNull(data);
        assertEquals(List.of(), data.get("items"));
    }

    private Model dbMapsOrderModel(String tableName) {
        Model customer = new Model();
        customer.setProperties(List.of(
                simpleProperty("customerId", PropertyType.Long),
                simpleProperty("displayName", PropertyType.String)));

        Model order = new Model();
        order.setTableName(tableName);
        Property orderId = columnProperty("orderId", "ORDER_ID", PropertyType.String);
        order.setIdProperty(orderId);
        Property customerSnapshot = simpleProperty("customer", PropertyType.BusinessObject);
        customerSnapshot.setPropertyModel(customer);
        customerSnapshot.setMultiMap(true);
        customerSnapshot.setDbMaps(List.of(
                new MultiDbMap("customerId", "CUSTOMER_ID"),
                new MultiDbMap("displayName", "CUSTOMER_NAME")));
        order.setProperties(List.of(orderId, customerSnapshot));
        return order;
    }

    private DbMysqlDynamic dbMapsOrderData(Model order, String orderId, Long customerId, String customerName) {
        DbMysqlDynamic customerData = new DbMysqlDynamic(order.getProperties().get(1).getPropertyModel());
        customerData.set("customerId", customerId);
        customerData.set("displayName", customerName);
        DbMysqlDynamic orderData = new DbMysqlDynamic(order);
        orderData.set("orderId", orderId);
        orderData.set("customer", customerData);
        return orderData;
    }

    private Property simpleProperty(String name, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setPropertyType(type);
        property.setIsCollection(false);
        property.setMultiMap(false);
        return property;
    }

    private Property columnProperty(String name, String column, PropertyType type) {
        Property property = simpleProperty(name, type);
        property.setColumn(column);
        return property;
    }

    private Property collectionProperty(String name) {
        Property property = simpleProperty(name, PropertyType.BusinessObject);
        property.setIsCollection(true);
        return property;
    }

    private void cleanupRuntimeEnumModel(long modelId, String modelName) {
        jdbcTemplate.update("DELETE FROM `fool_sys_model_enum` WHERE `owner` = ?", modelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", modelId, modelName);
    }

    private void createRuntimeDetailModel(
            long modelId,
            long idPropertyId,
            long namePropertyId,
            String modelName,
            String tableName) {
        jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "`ORDER_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ORDER_ID`))");
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                modelId,
                modelName,
                modelName,
                "runtime detail test",
                ModelType.DYNAMIC.code(),
                "example." + modelName,
                tableName,
                false,
                idPropertyId);
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`filter`,`format`,`column`,"
                        + "`property_type`,`allow_db_null`,`is_check`,`ix_group`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                idPropertyId,
                "orderId",
                "order id",
                null,
                false,
                modelId,
                null,
                null,
                "ORDER_ID",
                PropertyType.String.code(),
                false,
                true,
                "",
                false);
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`filter`,`format`,`column`,"
                        + "`property_type`,`allow_db_null`,`is_check`,`ix_group`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                namePropertyId,
                "orderName",
                "order name",
                null,
                false,
                modelId,
                null,
                null,
                "ORDER_NAME",
                PropertyType.String.code(),
                true,
                false,
                null,
                false);
    }

    private void cleanupRuntimeDetailModel(long modelId, String modelName, String tableName) {
        jdbcTemplate.update("DELETE FROM `fool_sys_model_property` WHERE `owner` = ?", modelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", modelId, modelName);
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
    }

    private void createRuntimeDbMapsTable(String tableName) {
        jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                + "`ORDER_ID` varchar(64) NOT NULL,"
                + "`CUSTOMER_ID` bigint DEFAULT NULL,"
                + "`CUSTOMER_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`ORDER_ID`))");
    }

    private void cleanupRuntimeDbMapsTable(String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
    }
}
