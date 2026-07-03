package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QueryContextTest {

    @Test
    public void addFirstTableCreatesLegacySelectedTablesWithShowNameAlias() {
        QueryTable orders = new QueryTable("Orders", "orders");
        QueryContext context = new QueryContext((table, joinType) -> List.of());

        context.add(orders);

        assertEquals(1, context.getInstance().getSelectedTables().getTables().size());
        assertEquals(orders, context.getInstance().getSelectedTables().getTables().get(0).getTable());
        assertEquals("Orders", context.getInstance().getSelectedTables().getTables().get(0).getSelectedTableName());
    }

    @Test
    public void addJoinedTableUsesLegacyDuplicateAliasSuffix() {
        QueryTable orders = new QueryTable("Orders", "orders");
        QueryTable items = new QueryTable("Items", "items");
        JoinTable factoryJoin = join(orders, items, "ID", "ORDER_ID");
        QueryContext context = new QueryContext((table, joinType) -> List.of(factoryJoin));
        context.add(orders);
        SelectedTable from = context.getInstance().getSelectedTables().getTables().get(0);

        context.add(items, from);
        context.add(items, from);

        assertEquals("Items", context.getInstance().getSelectedTables().getTables().get(1).getSelectedTableName());
        assertEquals("Items1", context.getInstance().getSelectedTables().getTables().get(2).getSelectedTableName());
    }

    @Test
    public void clearReplacesLegacyQueryInstance() {
        QueryContext context = new QueryContext((table, joinType) -> List.of());
        context.add(new QueryTable("Orders", "orders"));
        QueryInstance first = context.getInstance();

        context.clear();

        assertNotSame(first, context.getInstance());
        assertEquals(0, context.getInstance().getSelectedColumns().size());
    }

    @Test
    public void constructorKeepsLegacyQueryConnectionStringAcrossClear() {
        QueryContext context = new QueryContext((table, joinType) -> List.of(), "Server=legacy;Database=car_wash");
        context.add(new QueryTable("Orders", "orders"));

        context.clear();

        assertEquals("Server=legacy;Database=car_wash", context.getQueryConnectionString());
        assertEquals(0, context.getInstance().getSelectedColumns().size());
    }

    @Test
    public void canJoinSelectedStateBelongsToLegacyContextNotInstance() {
        QueryContext context = new QueryContext((table, joinType) -> List.of());

        assertEquals(false, context.isCanJoinSelected());

        context.setCanJoinSelected(true);
        context.clear();

        assertEquals(true, context.isCanJoinSelected());
    }

    @Test
    public void saveKeepsLegacyNotImplementedSurface() {
        QueryContext context = new QueryContext((table, joinType) -> List.of());

        try {
            context.save();
            fail("expected save to preserve legacy NotImplemented surface");
        } catch (UnsupportedOperationException ex) {
            assertEquals("NotImplementedException", ex.getMessage());
        }
    }

    @Test
    public void getResultDelegatesToPagedExecutorUsingCurrentInstance() {
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate(
                1L,
                List.of(Map.of("order_id", "1001")));
        QueryContext context = new QueryContext((table, joinType) -> List.of(), new JdbcQueryExecutor(jdbcTemplate));
        QueryTable orders = new QueryTable("Orders", "orders");
        context.add(orders);
        SelectedTable selectedOrders = context.getInstance().getSelectedTables().getTables().get(0);
        context.getInstance().getSelectedColumns().add(selectedColumn(selectedOrders));
        context.getInstance().setBoolExp(new CompareFilter("ORDER_ID", CompareOp.EQUAL, "1001"));

        QueryResult result = context.getResult(20, 2);

        assertEquals(2, result.getCurrentPage());
        assertEquals(20, result.getPageSize());
        assertEquals(1L, result.getTotalRecords());
        assertEquals(List.of(Map.of("order_id", "1001")), result.getRows());
        assertArrayEquals(new Object[]{"1001"}, jdbcTemplate.countArgs);
        assertArrayEquals(new Object[]{"1001", 2, 20, 2, 20}, jdbcTemplate.pageArgs);
    }

    @Test
    public void getResultWithoutExecutorOrConnectionStringKeepsRequiredExecutorError() {
        QueryContext context = new QueryContext((table, joinType) -> List.of(), (JdbcQueryExecutor) null);

        try {
            context.getResult(20);
            fail("expected missing executor to keep the legacy runtime error");
        } catch (IllegalStateException ex) {
            assertEquals("JdbcQueryExecutor is required to execute a QueryContext", ex.getMessage());
        }
    }

    @Test
    public void getResultKeepsLegacyConnectionStringOverload() {
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate(
                1L,
                List.of(Map.of("order_id", "1001")));
        QueryContext context = new QueryContext(
                (table, joinType) -> List.of(),
                new JdbcQueryExecutor(jdbcTemplate),
                "Server=default;Database=car_wash");
        QueryTable orders = new QueryTable("Orders", "orders");
        context.add(orders);
        SelectedTable selectedOrders = context.getInstance().getSelectedTables().getTables().get(0);
        context.getInstance().getSelectedColumns().add(selectedColumn(selectedOrders));

        QueryResult result = context.getResult("Server=runtime;Database=car_wash", 15);

        assertEquals(1, result.getCurrentPage());
        assertEquals(15, result.getPageSize());
        assertEquals("Server=default;Database=car_wash", context.getQueryConnectionString());
        assertArrayEquals(new Object[]{1, 15, 1, 15}, jdbcTemplate.pageArgs);
    }

    @Test
    public void getResultConnectionStringOverloadUsesRuntimeConnectionFactory() {
        RecordingJdbcTemplate defaultTemplate = new RecordingJdbcTemplate(
                1L,
                List.of(Map.of("order_id", "default")));
        RecordingJdbcTemplate runtimeTemplate = new RecordingJdbcTemplate(
                1L,
                List.of(Map.of("order_id", "runtime")));
        List<String> usedConnections = new ArrayList<>();
        QueryContext context = new QueryContext(
                (table, joinType) -> List.of(),
                connectionString -> {
                    usedConnections.add(connectionString);
                    return new JdbcQueryExecutor("runtime".equals(connectionString) ? runtimeTemplate : defaultTemplate);
                },
                "default");
        QueryTable orders = new QueryTable("Orders", "orders");
        context.add(orders);
        SelectedTable selectedOrders = context.getInstance().getSelectedTables().getTables().get(0);
        context.getInstance().getSelectedColumns().add(selectedColumn(selectedOrders));

        QueryResult result = context.getResult("runtime", 15);

        assertEquals(List.of("runtime"), usedConnections);
        assertEquals(List.of(Map.of("order_id", "runtime")), result.getRows());
        assertEquals(null, defaultTemplate.pageArgs);
    }

    @Test
    public void getResultLoadsLegacyEnumStateValuesBeforeExecuting() {
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate(
                1L,
                List.of(Map.of("status_name", "Ready")));
        QueryTable orders = new QueryTable("Orders", "orders");
        QueryFactory factory = new QueryFactory() {
            @Override
            public List<JoinTable> getCanJoinedTables(QueryTable table, JoinQueryType joinType) {
                return List.of();
            }

            @Override
            public List<ColStateValue> getStateValues(QueryColumn col) {
                assertEquals("STATUS", col.getDbName());
                ColStateValue ready = new ColStateValue();
                ready.setDbName("1");
                ready.setShowName("Ready");
                return List.of(ready);
            }
        };
        QueryContext context = new QueryContext(factory, new JdbcQueryExecutor(jdbcTemplate));
        context.add(orders);
        SelectedTable selectedOrders = context.getInstance().getSelectedTables().getTables().get(0);
        context.getInstance().getSelectedColumns().add(enumSelectedColumn(selectedOrders));

        context.getResult(20);

        assertEquals(1, context.getInstance().getSelectedColumns().get(0).getValues().size());
        assertTrue(jdbcTemplate.pageSql.contains("WHEN [Orders].[STATUS]=1 THEN 'Ready'"));
    }

    @Test
    public void getSqlReturnsLegacyNonPagedSelectAndLoadsEnumStateValues() {
        QueryTable orders = new QueryTable("Orders", "orders");
        QueryFactory factory = new QueryFactory() {
            @Override
            public List<JoinTable> getCanJoinedTables(QueryTable table, JoinQueryType joinType) {
                return List.of();
            }

            @Override
            public List<ColStateValue> getStateValues(QueryColumn col) {
                ColStateValue ready = new ColStateValue();
                ready.setDbName("1");
                ready.setShowName("Ready");
                return List.of(ready);
            }
        };
        QueryContext context = new QueryContext(factory);
        context.add(orders);
        SelectedTable selectedOrders = context.getInstance().getSelectedTables().getTables().get(0);
        context.getInstance().getSelectedColumns().add(enumSelectedColumn(selectedOrders));

        String sql = context.getSql("idx");

        assertEquals(1, context.getInstance().getSelectedColumns().get(0).getValues().size());
        assertEquals("SELECT distinct (CASE WHEN [Orders].[STATUS]=1 THEN 'Ready'  ELSE '' END) AS [status_name],"
                + "ROW_NUMBER() OVER  (ORDER BY  [Orders].[STATUS] ASC) AS [idx]"
                + " FROM [orders] as [Orders]", sql);
    }

    private SelectedColumn selectedColumn(SelectedTable selectedTable) {
        QueryColumn queryColumn = new QueryColumn();
        queryColumn.setTable(selectedTable.getTable());
        queryColumn.setShowName("订单号");
        queryColumn.setDbName("ORDER_ID");
        queryColumn.setDataType(PropertyType.Long);

        SelectType selectType = new SelectType();
        selectType.setDbExp("[{0}].[{1}]");

        SelectedColumn selectedColumn = new SelectedColumn("order_id", queryColumn);
        selectedColumn.setSelectedTable(selectedTable);
        selectedColumn.setSelectType(selectType);
        return selectedColumn;
    }

    private SelectedColumn enumSelectedColumn(SelectedTable selectedTable) {
        QueryColumn queryColumn = new QueryColumn();
        queryColumn.setTable(selectedTable.getTable());
        queryColumn.setShowName("状态");
        queryColumn.setDbName("STATUS");
        queryColumn.setDataType(PropertyType.Enum);

        SelectType selectType = new SelectType();
        selectType.setDbExp("[{0}].[{1}]");

        SelectedColumn selectedColumn = new SelectedColumn("status_name", queryColumn);
        selectedColumn.setSelectedTable(selectedTable);
        selectedColumn.setSelectType(selectType);
        return selectedColumn;
    }

    private JoinTable join(SelectedTable left, SelectedTable right, String leftCol, String rightCol) {
        JoinTable join = new JoinTable();
        join.setLeftTable(left);
        join.setRightTable(right);
        join.getConditions().add(new JoinCondition(leftCol, rightCol));
        return join;
    }

    private JoinTable join(QueryTable left, QueryTable right, String leftCol, String rightCol) {
        return join(new SelectedTable(left, left.getShowName()), new SelectedTable(right, right.getShowName()), leftCol, rightCol);
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
