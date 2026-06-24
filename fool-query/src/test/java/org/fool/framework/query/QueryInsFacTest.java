package org.fool.framework.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryInsFacTest {

    @Test
    public void refreshQueryInsReportParamMaterializesReportParametersFromBoolExpression() {
        QueryInstance instance = new QueryInstance();
        instance.setBoolExp(new CompareFilter("ORDER_ID", CompareOp.EQUAL, "1001")
                .and(new ReportCompareFilter(
                        instance,
                        "STATUS",
                        CompareOp.EQUAL,
                        "READY",
                        "Ready",
                        "state")));

        new QueryInsFac().refreshQueryInsReportParam(instance);

        assertEquals(1, instance.getReportParams().size());
        assertEquals("state", instance.getReportParams().get(0).getName());
        assertEquals("@p1", instance.getReportParams().get(0).getExp());
        assertEquals("READY", instance.getReportParams().get(0).getValue());
        assertEquals("Ready", instance.getReportParams().get(0).getFmtValue());
    }

    @Test
    public void refreshQueryInsReportParamKeepsExistingParameterValues() {
        QueryInstance instance = new QueryInstance();
        instance.getReportParams().add(new ReportParameter("state", "@state", "DONE", "Done"));
        instance.setBoolExp(new ReportCompareFilter(
                instance,
                "STATUS",
                CompareOp.EQUAL,
                "READY",
                "Ready",
                "state"));

        new QueryInsFac().refreshQueryInsReportParam(instance);

        assertEquals(1, instance.getReportParams().size());
        assertEquals("@state", instance.getReportParams().get(0).getExp());
        assertEquals("DONE", instance.getReportParams().get(0).getValue());
        assertEquals("Done", instance.getReportParams().get(0).getFmtValue());
    }
}
