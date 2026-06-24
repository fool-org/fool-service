package org.fool.framework.query;

import org.fool.framework.dao.QueryAndArgs;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ReportCompareFilterTest {

    @Test
    public void reportCompareFilterCreatesLegacyReportParameterWhenMissing() {
        QueryInstance instance = new QueryInstance();
        ReportCompareFilter filter = new ReportCompareFilter(
                instance,
                "STATUS",
                CompareOp.EQUAL,
                "READY",
                "Ready",
                "state");

        QueryAndArgs sql = filter.generateSql(2);

        assertEquals("`STATUS`= ?", sql.getSql());
        assertArrayEquals(new Object[]{"READY"}, sql.getArgs());
        assertEquals(1, instance.getReportParams().size());
        assertEquals("state", instance.getReportParams().get(0).getName());
        assertEquals("@p2", instance.getReportParams().get(0).getExp());
        assertEquals("READY", instance.getReportParams().get(0).getValue());
        assertEquals("Ready", instance.getReportParams().get(0).getFmtValue());
    }

    @Test
    public void reportCompareFilterReusesExistingReportParameterValueByName() {
        QueryInstance instance = new QueryInstance();
        instance.getReportParams().add(new ReportParameter("state", "@p0", "READY", "Ready"));
        ReportCompareFilter filter = new ReportCompareFilter(
                instance,
                "STATUS",
                CompareOp.EQUAL,
                "DONE",
                "Done",
                "state");

        QueryAndArgs sql = filter.generateSql(3);

        assertEquals("`STATUS`= ?", sql.getSql());
        assertArrayEquals(new Object[]{"READY"}, sql.getArgs());
        assertEquals(1, instance.getReportParams().size());
        assertEquals("@p0", instance.getReportParams().get(0).getExp());
        assertEquals("READY", instance.getReportParams().get(0).getValue());
        assertEquals("Ready", instance.getReportParams().get(0).getFmtValue());
    }

    @Test
    public void compositeFilterPassesLegacyParameterIndexToReportFilters() {
        QueryInstance instance = new QueryInstance();
        IQueryFilter filter = new CompareFilter("ORDER_ID", CompareOp.EQUAL, "1001")
                .and(new ReportCompareFilter(
                        instance,
                        "STATUS",
                        CompareOp.EQUAL,
                        "READY",
                        "Ready",
                        "state"));

        QueryAndArgs sql = filter.generateSql();

        assertEquals("(`ORDER_ID`= ?) AND (`STATUS`= ?)", sql.getSql());
        assertArrayEquals(new Object[]{"1001", "READY"}, sql.getArgs());
        assertEquals("@p1", instance.getReportParams().get(0).getExp());
    }
}
