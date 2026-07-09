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
                    "runtime sysid create test",
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

    private void cleanup(long modelId, String modelName, String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + tableName + "`");
        jdbcTemplate.update("DELETE FROM `fool_sys_model_property` WHERE `owner` = ?", modelId);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", modelId, modelName);
    }
}
