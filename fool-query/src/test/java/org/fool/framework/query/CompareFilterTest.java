package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CompareFilterTest {

    @Test
    public void betweenFilterGeneratesLegacyRangeSqlWithOrderedParameters() {
        QueryAndArgs sql = new BetweenFilter("ORDER_DATE", "2026-01-01", "2026-01-31").generateSql();

        assertEquals("`ORDER_DATE` BETWEEN ? AND ?", sql.getSql());
        assertArrayEquals(new Object[]{"2026-01-01", "2026-01-31"}, sql.getArgs());
    }

    @Test
    public void inFilterClosesLegacyPlaceholderList() {
        QueryAndArgs sql = new InFilter("STATUS", "READY", "DONE").generateSql();

        assertEquals("`STATUS` IN (?, ?)", sql.getSql());
        assertArrayEquals(new Object[]{"READY", "DONE"}, sql.getArgs());
    }

    @Test
    public void compositeFilterKeepsLegacyExpressionParenthesesAndArgumentOrder() {
        IQueryFilter filter = new CompareFilter("STATUS", CompareOp.EQUAL, "READY")
                .and(new BetweenFilter("ORDER_DATE", "2026-01-01", "2026-01-31"))
                .or(new InFilter("OWNER_ID", "u1", "u2"));

        QueryAndArgs sql = filter.generateSql();

        assertEquals(
                "(`STATUS`= ?) And (`ORDER_DATE` BETWEEN ? AND ?) OR (`OWNER_ID` IN (?, ?))",
                sql.getSql());
        assertArrayEquals(
                new Object[]{"READY", "2026-01-01", "2026-01-31", "u1", "u2"},
                sql.getArgs());
    }

    @Test
    public void boolOpKeepsLegacyDisplayAndSqlTokens() {
        assertEquals(" And ", BoolOp.AND.getDbName());
        assertEquals("并且", BoolOp.AND.getShowName());
        assertEquals(" OR ", BoolOp.OR.getDbName());
        assertEquals("或者", BoolOp.OR.getShowName());
    }
}
