package org.fool.framework.model.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.Application;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
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
}
