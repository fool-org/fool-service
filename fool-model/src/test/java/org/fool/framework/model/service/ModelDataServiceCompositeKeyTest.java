package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.model.Application;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.query.IQueryFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class ModelDataServiceCompositeKeyTest {
    private static final long MODEL_ID = 91011L;
    private static final String MODEL_NAME = "RuntimeCompositeApplicationDatabase";
    private static final String TABLE_NAME = "runtime_composite_application_database";

    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getDataListDoesNotAssumeSysIdForCompositeKeyModel() {
        cleanup();
        try {
            createModel();
            jdbcTemplate.update(
                    "INSERT INTO `" + TABLE_NAME + "` (`APP_ID`,`DB_NO`) VALUES (?,?)",
                    1L,
                    "01");
            Model model = modelDataService.getModel(MODEL_NAME);
            PageNavigator page = new PageNavigator();
            page.setPageIndex(1);
            page.setPageSize(10);

            var result = modelDataService.getDataListWithPageInfo(
                    MODEL_NAME,
                    IQueryFilter.init(),
                    model.getProperties(),
                    page);

            assertEquals(1, result.getItems().size());
            assertEquals(1L, result.getItems().get(0).get("appId"));
            assertEquals("01", result.getItems().get(0).get("databaseNumber"));
            assertNull(result.getItems().get(0).getId());
        } finally {
            cleanup();
        }
    }

    private void createModel() {
        jdbcTemplate.execute("CREATE TABLE `" + TABLE_NAME + "` ("
                + "`APP_ID` bigint NOT NULL,"
                + "`DB_NO` varchar(32) NOT NULL,"
                + "PRIMARY KEY (`APP_ID`,`DB_NO`))");
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model` "
                        + "(`id`,`name`,`text`,`remark`,`model_type`,`class_name`,`table_name`,`auto_sys_id`,`id_property`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?)",
                MODEL_ID,
                MODEL_NAME,
                MODEL_NAME,
                "runtime composite-key list test",
                ModelType.DYNAMIC.code(),
                "example." + MODEL_NAME,
                TABLE_NAME,
                false,
                null);
        insertProperty(91012L, "appId", "APP_ID", PropertyType.Long);
        insertProperty(91013L, "databaseNumber", "DB_NO", PropertyType.String);
    }

    private void insertProperty(long id, String name, String column, PropertyType type) {
        jdbcTemplate.update(
                "INSERT INTO `fool_sys_model_property` "
                        + "(`id`,`name`,`remark`,`is_collection`,`owner`,`column`,`property_type`,`allow_db_null`,`is_check`,`multi_map`) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                id,
                name,
                name,
                false,
                MODEL_ID,
                column,
                type.code(),
                true,
                false,
                false);
    }

    private void cleanup() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS `" + TABLE_NAME + "`");
        jdbcTemplate.update("DELETE FROM `fool_sys_model_property` WHERE `owner` = ?", MODEL_ID);
        jdbcTemplate.update("DELETE FROM `fool_sys_model` WHERE `id` = ? OR `name` = ?", MODEL_ID, MODEL_NAME);
    }
}
