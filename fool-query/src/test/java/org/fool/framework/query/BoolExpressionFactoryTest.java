package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.QueryAndArgs;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BoolExpressionFactoryTest {

    @Test
    public void factoryCreatesSimpleExpressionWithFactoryOwnedQueryInstance() {
        QueryInstance instance = new QueryInstance();
        BoolExpressionFactory factory = new BoolExpressionFactory(instance);

        IQueryFilter filter = factory.createBoolExpression(
                compareCol("Orders", "STATUS"),
                CompareOp.EQUAL,
                "READY",
                "Ready",
                "state");

        QueryAndArgs sql = filter.generateSql(4);

        assertEquals("[Orders].[STATUS]= ?", sql.getSql());
        assertArrayEquals(new Object[]{"READY"}, sql.getArgs());
        assertEquals(1, instance.getReportParams().size());
        assertEquals("state", instance.getReportParams().get(0).getName());
        assertEquals("@p4", instance.getReportParams().get(0).getExp());
    }

    @Test
    public void factoryAddsExistingBoolExpressionInPlaceWithLegacyParameterOrdering() {
        QueryInstance instance = new QueryInstance();
        BoolExpressionFactory factory = new BoolExpressionFactory(instance);
        BoolExpression first = new BoolExpression(
                instance,
                factory.createBoolExpression(compareCol("Orders", "STATUS"),
                        CompareOp.EQUAL,
                        "READY",
                        "Ready",
                        "state"));
        BoolExpression second = new BoolExpression(
                instance,
                factory.createBoolExpression(compareCol("Orders", "AMOUNT"),
                        CompareOp.MORE,
                        "100",
                        "100",
                        "amount"));

        factory.addBoolExpression(first, BoolOp.AND, second);

        QueryAndArgs sql = first.getExp().generateSql(2);

        assertEquals("([Orders].[STATUS]= ?) And ([Orders].[AMOUNT]> ?)", sql.getSql());
        assertArrayEquals(new Object[]{"READY", "100"}, sql.getArgs());
        assertEquals("@p2", instance.getReportParams().get(0).getExp());
        assertEquals("@p3", instance.getReportParams().get(1).getExp());
    }

    @Test
    public void factoryAddsCompareColumnExpressionUsingBoolExpressionOwner() {
        QueryInstance factoryInstance = new QueryInstance();
        QueryInstance expressionInstance = new QueryInstance();
        BoolExpressionFactory factory = new BoolExpressionFactory(factoryInstance);
        BoolExpression first = new BoolExpression(
                expressionInstance,
                new SimpleBoolExpression(
                        compareCol("Orders", "STATUS"),
                        CompareOp.EQUAL,
                        "READY",
                        "Ready",
                        expressionInstance,
                        "state"));

        factory.addBoolExpression(
                first,
                BoolOp.OR,
                compareCol("Orders", "AMOUNT"),
                CompareOp.MORE,
                "100",
                "100",
                "amount");

        QueryAndArgs sql = first.getExp().generateSql(6);

        assertEquals("([Orders].[STATUS]= ?) OR ([Orders].[AMOUNT]> ?)", sql.getSql());
        assertEquals(0, factoryInstance.getReportParams().size());
        assertEquals(2, expressionInstance.getReportParams().size());
        assertEquals("@p6", expressionInstance.getReportParams().get(0).getExp());
        assertEquals("@p7", expressionInstance.getReportParams().get(1).getExp());
    }

    private CompareCol compareCol(String selectedTableName, String dbName) {
        QueryColumn column = new QueryColumn();
        column.setDbName(dbName);
        column.setShowName(dbName);
        column.setDataType(PropertyType.String);
        return new CompareCol(column, selectedTableName);
    }
}
