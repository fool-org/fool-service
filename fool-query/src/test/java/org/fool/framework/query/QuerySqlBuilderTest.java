package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.QueryAndArgs;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class QuerySqlBuilderTest {

    @Test
    public void tableSqlUsesLegacySelectedTableAliasesAndJoinConditions() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTable items = selectedTable("Items", "order_items", "i");
        JoinTable factoryJoin = join(orders, items, "ID", "ORDER_ID");

        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of(factoryJoin));

        selectedTables.add(items, orders);

        assertEquals("[orders] as [o] JOIN [order_items] as [i] ON 1=1 AND [o].[ID]=[i].[ORDER_ID]",
                QuerySqlBuilder.tableSql(selectedTables));
    }

    @Test
    public void selectedColumnSqlUsesLegacyExpressionAliasFormat() {
        SelectedColumn column = selectedColumn(
                selectedTable("Orders", "orders", "o"),
                "订单号",
                "ORDER_ID",
                PropertyType.Long,
                "order_id",
                selectType("[{0}].[{1}]", false));

        assertEquals(" [o].[ORDER_ID]  AS [order_id]", QuerySqlBuilder.selectedColumnSql(column));
    }

    @Test
    public void sqlBuilderDoesNotDoubleWrapLegacyBracketedIdentifiers() {
        SelectedTable orders = selectedTable("Orders", "[orders]", "[o]");
        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of());
        QueryInstance instance = new QueryInstance();
        instance.setSelectedTables(selectedTables);
        instance.getSelectedColumns().add(selectedColumn(
                orders,
                "订单号",
                "[ORDER_ID]",
                PropertyType.Long,
                "order_id",
                selectType("[{0}].[{1}]", false)));

        assertEquals("[orders] as [o]", QuerySqlBuilder.tableSql(selectedTables));
        assertEquals(
                "SELECT distinct  [o].[ORDER_ID]  AS [order_id],"
                        + "ROW_NUMBER() OVER  (ORDER BY  [o].[ORDER_ID] ASC) AS [RowIndex]"
                        + " FROM [orders] as [o]",
                QuerySqlBuilder.selectSql(instance));
    }

    @Test
    public void tableSqlDoesNotDoubleWrapLegacyBracketedJoinIdentifiers() {
        SelectedTable orders = selectedTable("Orders", "[orders]", "[o]");
        SelectedTable items = selectedTable("Items", "[order_items]", "[i]");
        JoinTable factoryJoin = join(orders, items, "[ID]", "[ORDER_ID]");
        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of(factoryJoin));

        selectedTables.add(items, orders);

        assertEquals("[orders] as [o] JOIN [order_items] as [i] ON 1=1 AND [o].[ID]=[i].[ORDER_ID]",
                QuerySqlBuilder.tableSql(selectedTables));
    }

    @Test
    public void selectedColumnSqlExpandsEnumValuesToLegacyCaseExpression() {
        SelectedColumn column = selectedColumn(
                selectedTable("Orders", "orders", "o"),
                "状态",
                "STATE",
                PropertyType.Enum,
                "state_text",
                selectType("[{0}].[{1}]", false));
        column.getValues().add(stateValue("待处理", "1"));
        column.getValues().add(stateValue("完成", "2"));

        assertEquals("(CASE WHEN [o].[STATE]=1 THEN '待处理'  WHEN [o].[STATE]=2 THEN '完成'  ELSE '' END) AS [state_text]",
                QuerySqlBuilder.selectedColumnSql(column));
    }

    @Test
    public void selectSqlBuildsLegacyDistinctProjectionRowNumberJoinAndGroupBy() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTable items = selectedTable("Items", "order_items", "i");
        JoinTable factoryJoin = join(orders, items, "ID", "ORDER_ID");
        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of(factoryJoin));
        selectedTables.add(items, orders);

        SelectedColumn orderId = selectedColumn(
                orders,
                "订单号",
                "ORDER_ID",
                PropertyType.Long,
                "order_id",
                selectType("[{0}].[{1}]", false));
        orderId.setOrderType(OrderType.ASC);
        SelectedColumn total = selectedColumn(
                items,
                "金额",
                "AMOUNT",
                PropertyType.Decimal,
                "total",
                selectType("SUM([{0}].[{1}])", true));

        QueryInstance instance = new QueryInstance();
        instance.setSelectedTables(selectedTables);
        instance.getSelectedColumns().add(orderId);
        instance.getSelectedColumns().add(total);

        assertEquals(
                "SELECT distinct  [o].[ORDER_ID]  AS [order_id], SUM([i].[AMOUNT])  AS [total],"
                        + "ROW_NUMBER() OVER  (ORDER BY  [o].[ORDER_ID] ASC) AS [RowIndex]"
                        + " FROM [orders] as [o] JOIN [order_items] as [i] ON 1=1 AND [o].[ID]=[i].[ORDER_ID]"
                        + " GROUP BY [o].[ORDER_ID]",
                QuerySqlBuilder.selectSql(instance));
    }

    @Test
    public void pagedSqlWrapsBaseSelectWithLegacyCountAndPageProjection() {
        QueryInstance instance = queryInstanceWithSingleOrderIdColumn();
        String baseSql = QuerySqlBuilder.selectSql(instance);

        QueryAndArgs sql = QuerySqlBuilder.pagedSql(instance, 20, 2);

        assertEquals(
                "SELECT COUNT(*) FROM (" + baseSql + ")A\n"
                        + "            SELECT  [order_id]  FROM (" + baseSql + ")A"
                        + " WHERE RowIndex>(? -1) * ? AND RowIndex<=?*? ORDER BY RowIndex\n"
                        + "            ",
                sql.getSql());
        assertArrayEquals(new Object[]{2, 20, 2, 20}, sql.getArgs());
    }

    @Test
    public void pagedSqlKeepsFilterArgsBeforePagingArgsAndCanIncludeRowIndex() {
        QueryInstance instance = queryInstanceWithSingleOrderIdColumn();
        instance.setBoolExp(new CompareFilter("ORDER_ID", CompareOp.EQUAL, "1001"));
        String baseSql = QuerySqlBuilder.selectSql(instance, "idx");

        QueryAndArgs sql = QuerySqlBuilder.pagedSql(instance, 10, 3, "idx", true);

        assertEquals(
                "SELECT COUNT(*) FROM (" + baseSql + ")A\n"
                        + "            SELECT  [order_id],[idx]  FROM (" + baseSql + ")A"
                        + " WHERE idx>(? -1) * ? AND idx<=?*? ORDER BY idx\n"
                        + "            ",
                sql.getSql());
        assertArrayEquals(new Object[]{"1001", "1001", 3, 10, 3, 10}, sql.getArgs());
    }

    @Test
    public void pagedSqlBindsLegacyReportParamsReferencedByName() {
        QueryInstance instance = queryInstanceWithSingleOrderIdColumn();
        instance.setBoolExp(rawFilter("`STATUS`=@state"));
        instance.getReportParams().add(new ReportParameter("state", "state", "READY", "Ready"));
        String baseSql = QuerySqlBuilder.selectSql(instance);

        QueryAndArgs sql = QuerySqlBuilder.pagedSql(instance, 10, 1);

        assertEquals(
                "SELECT COUNT(*) FROM (" + baseSql + ")A\n"
                        + "            SELECT  [order_id]  FROM (" + baseSql + ")A"
                        + " WHERE RowIndex>(? -1) * ? AND RowIndex<=?*? ORDER BY RowIndex\n"
                        + "            ",
                sql.getSql());
        assertEquals("SELECT distinct  [o].[ORDER_ID]  AS [order_id],"
                + "ROW_NUMBER() OVER  (ORDER BY  [o].[ORDER_ID] ASC) AS [RowIndex]"
                + " FROM [orders] as [o] WHERE `STATUS`=?", baseSql);
        assertArrayEquals(new Object[]{"READY", "READY", 1, 10, 1, 10}, sql.getArgs());
    }

    private SelectedTable selectedTable(String showName, String dbName, String selectedName) {
        return new SelectedTable(new QueryTable(showName, dbName), selectedName);
    }

    private JoinTable join(SelectedTable left, SelectedTable right, String leftCol, String rightCol) {
        JoinTable join = new JoinTable();
        join.setLeftTable(left);
        join.setRightTable(right);
        join.getConditions().add(new JoinCondition(leftCol, rightCol));
        return join;
    }

    private SelectedColumn selectedColumn(
            SelectedTable selectedTable,
            String showName,
            String dbName,
            PropertyType dataType,
            String selectedName,
            SelectType selectType) {
        QueryColumn queryColumn = new QueryColumn();
        queryColumn.setTable(selectedTable.getTable());
        queryColumn.setShowName(showName);
        queryColumn.setDbName(dbName);
        queryColumn.setDataType(dataType);

        SelectedColumn column = new SelectedColumn(selectedName, queryColumn);
        column.setSelectedTable(selectedTable);
        column.setSelectType(selectType);
        return column;
    }

    private SelectType selectType(String dbExp, boolean requireGroupCol) {
        SelectType selectType = new SelectType();
        selectType.setDbExp(dbExp);
        selectType.setRequireGroupCol(requireGroupCol);
        return selectType;
    }

    private ColStateValue stateValue(String showName, String dbName) {
        ColStateValue value = new ColStateValue();
        value.setShowName(showName);
        value.setDbName(dbName);
        return value;
    }

    private IQueryFilter rawFilter(String sql) {
        return new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs queryAndArgs = new QueryAndArgs();
                queryAndArgs.setSql(sql);
                queryAndArgs.setArgs(new Object[]{});
                return queryAndArgs;
            }
        };
    }

    private QueryInstance queryInstanceWithSingleOrderIdColumn() {
        SelectedTable orders = selectedTable("Orders", "orders", "o");
        SelectedTables selectedTables = new SelectedTables(
                orders,
                (table, joinType) -> List.of());
        SelectedColumn orderId = selectedColumn(
                orders,
                "订单号",
                "ORDER_ID",
                PropertyType.Long,
                "order_id",
                selectType("[{0}].[{1}]", false));

        QueryInstance instance = new QueryInstance();
        instance.setSelectedTables(selectedTables);
        instance.getSelectedColumns().add(orderId);
        return instance;
    }
}
