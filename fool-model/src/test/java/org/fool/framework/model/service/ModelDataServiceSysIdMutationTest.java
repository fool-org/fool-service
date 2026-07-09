package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class ModelDataServiceSysIdMutationTest {
    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void createDataWritesSysIdWhenModelHasNoIdProperty() {
        long modelId = 97001L;
        long namePropertyId = 97002L;
        String modelName = "RuntimeCreateSysIdObject";
        String tableName = "runtime_create_sysid_object";
        cleanup(modelId, modelName, tableName);
        try {
            createSysIdModel(modelId, namePropertyId, modelName, tableName);
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("SYSID", "9701");
            data.set("objectName", "Created object");

            assertEquals(Boolean.TRUE, modelDataService.createData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `OBJECT_NAME` FROM `" + tableName + "` WHERE `SYSID` = ?",
                    String.class,
                    "9701");
            assertEquals("Created object", name);
        } finally {
            cleanup(modelId, modelName, tableName);
        }
    }

    @Test
    public void saveDataUsesOldSysIdWhenModelHasNoIdProperty() {
        long modelId = 97011L;
        long namePropertyId = 97012L;
        String modelName = "RuntimeSaveOldSysIdObject";
        String tableName = "runtime_save_old_sysid_object";
        cleanup(modelId, modelName, tableName);
        try {
            createSysIdModel(modelId, namePropertyId, modelName, tableName);
            jdbcTemplate.update(
                    "INSERT INTO `" + tableName + "` (`SYSID`,`OBJECT_NAME`) VALUES (?,?)",
                    "9702",
                    "Before save");
            Model model = modelDataService.getModel(modelName);
            DbMysqlDynamic data = new DbMysqlDynamic(model);
            data.set("SYSID", "9702");
            data.set("SYSID", "9703");
            data.set("objectName", "After save");

            assertEquals(Boolean.TRUE, modelDataService.saveData(data));

            String name = jdbcTemplate.queryForObject(
                    "SELECT `OBJECT_NAME` FROM `" + tableName + "` WHERE `SYSID` = ?",
                    String.class,
                    "9702");
            Integer changedIdCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM `" + tableName + "` WHERE `SYSID` = ?",
                    Integer.class,
                    "9703");
            assertEquals("After save", name);
            assertEquals(0, changedIdCount.intValue());
        } finally {
            cleanup(modelId, modelName, tableName);
        }
    }

    private void createSysIdModel(long modelId, long namePropertyId, String modelName, String tableName) {
        jdbcTemplate.execute("CREATE TABLE `" + tableName + "` ("
                + "`SYSID` varchar(64) NOT NULL,"
                + "`OBJECT_NAME` varchar(255) DEFAULT NULL,"
                + "PRIMARY KEY (`SYSID`))");
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                modelId,
                modelName,
                modelName,
                "runtime sysid mutation test",
                ModelType.DYNAMIC.code(),
                "example." + modelName,
                tableName,
                false,
                null);
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`filter`,`format`,`column`,"
                        + "`property_type`,`allow_db_null`,`is_check`,`ix_group`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                namePropertyId,
                "objectName",
                "object name",
                null,
                false,
                modelId,
                null,
                null,
                "OBJECT_NAME",
                PropertyType.String.code(),
                true,
                false,
                null,
                false);
    }

    private void cleanup(long modelId, String modelName, String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        jdbcTemplate.update("DELETE FROM `fool_sys_model_property` WHERE `owner` = ?", modelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", modelId, modelName);
    }
}
