package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.QueryAndArgs;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SimpleBoolExpressionTest {

    @Test
    public void simpleBoolExpressionUsesLegacySelectedTableColumnAndReportParameterIndex() {
        QueryInstance instance = new QueryInstance();
        SimpleBoolExpression expression = new SimpleBoolExpression(
                compareCol("Orders", "STATUS"),
                CompareOp.EQUAL,
                "READY",
                "Ready",
                instance,
                "state");

        QueryAndArgs sql = expression.generateSql(3);

        assertEquals("[Orders].[STATUS]= ?", sql.getSql());
        assertArrayEquals(new Object[]{"READY"}, sql.getArgs());
        assertEquals(1, instance.getReportParams().size());
        assertEquals("state", instance.getReportParams().get(0).getName());
        assertEquals("@p3", instance.getReportParams().get(0).getExp());
        assertEquals("READY", instance.getReportParams().get(0).getValue());
        assertEquals("Ready", instance.getReportParams().get(0).getFmtValue());
    }

    @Test
    public void simpleBoolExpressionFormatsLegacyDisplayString() {
        SimpleBoolExpression expression = new SimpleBoolExpression(
                compareCol("Orders", "STATUS", "状态"),
                CompareOp.EQUAL,
                "READY",
                "就绪",
                new QueryInstance(),
                "state");

        assertEquals("Orders.状态 等于 就绪", expression.toString());
    }

    private CompareCol compareCol(String selectedTableName, String dbName) {
        return compareCol(selectedTableName, dbName, dbName);
    }

    private CompareCol compareCol(String selectedTableName, String dbName, String showName) {
        QueryColumn column = new QueryColumn();
        column.setDbName(dbName);
        column.setShowName(showName);
        column.setDataType(PropertyType.String);
        return new CompareCol(column, selectedTableName);
    }
}
