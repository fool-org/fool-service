package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JdbcQueryExecutorTest {

    @Test
    public void executeRunsCountAndPageQueriesAndMapsLegacyQueryResult() {
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate(
                21L,
                List.of(Map.of("order_id", "1001")));
        JdbcQueryExecutor executor = new JdbcQueryExecutor(jdbcTemplate);
        QueryInstance instance = queryInstance();
        instance.setBoolExp(new CompareFilter("ORDER_ID", CompareOp.EQUAL, "1001"));

        QueryResult result = executor.execute(instance, 10, 2);

        assertEquals(2, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(21L, result.getTotalRecords());
        assertEquals(3L, result.getTotalPages());
        assertEquals(jdbcTemplate.rows, result.getRows());

        assertTrue(jdbcTemplate.countSql.startsWith("SELECT COUNT(*) FROM (SELECT distinct"));
        assertTrue(jdbcTemplate.pageSql.startsWith("SELECT  [order_id]  FROM (SELECT distinct"));
        assertArrayEquals(new Object[]{"1001"}, jdbcTemplate.countArgs);
        assertArrayEquals(new Object[]{"1001", 2, 10, 2, 10}, jdbcTemplate.pageArgs);
    }

    @Test
    public void parsesLegacyConnectionStringsForRuntimeExecutors() {
        JdbcQueryExecutor.ConnectionSettings settings = JdbcQueryExecutor.parse(
                "Data Source=legacy-db;Initial Catalog=LegacyApp;User ID=app_user;Password=secret");

        assertEquals("jdbc:sqlserver://legacy-db;databaseName=LegacyApp", settings.url());
        assertEquals("app_user", settings.username());
        assertEquals("secret", settings.password());
        assertEquals(null, settings.driverClassName());
    }

    private QueryInstance queryInstance() {
        SelectedTable orders = new SelectedTable(new QueryTable("Orders", "orders"), "o");
        SelectedTables selectedTables = new SelectedTables(orders, (table, joinType) -> List.of());

        QueryColumn queryColumn = new QueryColumn();
        queryColumn.setTable(orders.getTable());
        queryColumn.setShowName("订单号");
        queryColumn.setDbName("ORDER_ID");
        queryColumn.setDataType(PropertyType.Long);

        SelectType selectType = new SelectType();
        selectType.setDbExp("[{0}].[{1}]");

        SelectedColumn selectedColumn = new SelectedColumn("order_id", queryColumn);
        selectedColumn.setSelectedTable(orders);
        selectedColumn.setSelectType(selectType);

        QueryInstance instance = new QueryInstance();
        instance.setSelectedTables(selectedTables);
        instance.getSelectedColumns().add(selectedColumn);
        return instance;
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {
        private final Long count;
        private final List<Map<String, Object>> rows;
        private String countSql;
        private String pageSql;
        private Object[] countArgs;
        private Object[] pageArgs;

        private RecordingJdbcTemplate(Long count, List<Map<String, Object>> rows) {
            this.count = count;
            this.rows = rows;
        }

        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            this.countSql = sql;
            this.countArgs = Arrays.copyOf(args, args.length);
            return requiredType.cast(count);
        }

        @Override
        public List<Map<String, Object>> queryForList(String sql, Object... args) {
            this.pageSql = sql;
            this.pageArgs = Arrays.copyOf(args, args.length);
            return rows;
        }
    }
}
