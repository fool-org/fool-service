package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.Application;
import org.fool.framework.model.model.ModelType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class ModelDataServiceJoinedDetailTest {
    private static final long CUSTOMER_MODEL_ID = 91021L;
    private static final long ORDER_MODEL_ID = 91024L;
    private static final String CUSTOMER_MODEL = "RuntimeJoinedDetailCustomer";
    private static final String ORDER_MODEL = "RuntimeJoinedDetailOrder";
    private static final String CUSTOMER_TABLE = "runtime_joined_detail_customer";
    private static final String ORDER_TABLE = "runtime_joined_detail_order";

    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getOneDataQualifiesIdentityWhenBusinessObjectJoinsShareTheColumn() {
        cleanup();
        try {
            createModels();
            jdbcTemplate.update(
                    "INSERT INTO `" + CUSTOMER_TABLE + "` (`id`,`name`) VALUES (?,?)",
                    7L,
                    "Ada");
            jdbcTemplate.update(
                    "INSERT INTO `" + ORDER_TABLE + "` (`id`,`customer_id`) VALUES (?,?)",
                    1001L,
                    7L);

            var order = modelDataService.getOneData(ORDER_MODEL, "1001");

            assertNotNull(order);
            assertEquals("1001", order.getId());
        } finally {
            cleanup();
        }
    }

    private void createModels() {
        jdbcTemplate.execute("CREATE TABLE `" + CUSTOMER_TABLE + "` ("
                + "`id` bigint NOT NULL PRIMARY KEY,`name` varchar(64) NOT NULL)");
        jdbcTemplate.execute("CREATE TABLE `" + ORDER_TABLE + "` ("
                + "`id` bigint NOT NULL PRIMARY KEY,`customer_id` bigint NOT NULL)");
        insertModel(CUSTOMER_MODEL_ID, CUSTOMER_MODEL, CUSTOMER_TABLE, 91022L);
        insertProperty(91022L, CUSTOMER_MODEL_ID, "id", "id", PropertyType.Long, null);
        insertProperty(91023L, CUSTOMER_MODEL_ID, "name", "name", PropertyType.String, null);
        insertModel(ORDER_MODEL_ID, ORDER_MODEL, ORDER_TABLE, 91025L);
        insertProperty(91025L, ORDER_MODEL_ID, "id", "id", PropertyType.Long, null);
        insertProperty(91026L, ORDER_MODEL_ID, "customer", "customer_id", PropertyType.BusinessObject, CUSTOMER_MODEL_ID);
    }

    private void insertModel(long id, String name, String table, long idProperty) {
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                id,
                name,
                name,
                "joined detail identity test",
                ModelType.DYNAMIC.code(),
                "example." + name,
                table,
                false,
                idProperty);
    }

    private void insertProperty(
            long id,
            long owner,
            String name,
            String column,
            PropertyType type,
            Long propertyModel) {
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`property_model`,`is_collection`,`owner`,`column`,`property_type`,"
                        + "`allow_db_null`,`is_check`,`multi_map`) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                id,
                name,
                name,
                propertyModel,
                false,
                owner,
                column,
                type.code(),
                true,
                false,
                false);
    }

    private void cleanup() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + ORDER_TABLE + "`");
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + CUSTOMER_TABLE + "`");
        jdbcTemplate.update(
                "DELETE FROM `fool_sys_model_property` WHERE `owner` IN (?,?)",
                ORDER_MODEL_ID,
                CUSTOMER_MODEL_ID);
        jdbcTemplate.update(
                "DELETE FROM `fool_sys_model` WHERE `id` IN (?,?) OR `name` IN (?,?)",
                ORDER_MODEL_ID,
                CUSTOMER_MODEL_ID,
                ORDER_MODEL,
                CUSTOMER_MODEL);
    }
}
