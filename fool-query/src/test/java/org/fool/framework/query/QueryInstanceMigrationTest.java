package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryInstanceMigrationTest {

    @Test
    public void queryInstanceInitializesLegacyCollections() {
        QueryInstance instance = new QueryInstance();

        assertEquals(0, instance.getSelectedColumns().size());
        assertTrue(instance.getParams().isEmpty());
        assertTrue(instance.getReportParams().isEmpty());
    }

    @Test
    public void queryAndReportParametersKeepLegacyState() {
        QueryColumn column = column("订单号", "ORDER_ID");
        CompareCol compareCol = new CompareCol(column, "o");
        QueryParameter queryParameter = new QueryParameter("p0", compareCol, 1001L);
        ReportParameter reportParameter = new ReportParameter("state", "@state", "OPEN", "Open");

        QueryInstance instance = new QueryInstance();
        instance.getParams().add(queryParameter);
        instance.getReportParams().add(reportParameter);

        assertEquals("p0", instance.getParams().get(0).getName());
        assertEquals(column, instance.getParams().get(0).getColumn().getCol());
        assertEquals("o", instance.getParams().get(0).getColumn().getSelectedTableName());
        assertEquals(1001L, instance.getParams().get(0).getValue());

        assertEquals("state", instance.getReportParams().get(0).getName());
        assertEquals("@state", instance.getReportParams().get(0).getExp());
        assertEquals("OPEN", instance.getReportParams().get(0).getValue());
        assertEquals("Open", instance.getReportParams().get(0).getFmtValue());
    }

    @Test
    public void queryResultComputesLegacyPageCountFromTotalRecords() {
        QueryResult result = new QueryResult(20);

        result.updatePage(41, List.of(
                Map.of("ORDER_ID", 1001L),
                Map.of("ORDER_ID", 1002L)));

        assertEquals(1, result.getCurrentPage());
        assertEquals(20, result.getPageSize());
        assertEquals(41L, result.getTotalRecords());
        assertEquals(3L, result.getTotalPages());
        assertEquals(2, result.getRows().size());

        result.setCurrentPage(3);
        result.updatePage(40, List.of());

        assertEquals(3, result.getCurrentPage());
        assertEquals(2L, result.getTotalPages());
    }

    private QueryColumn column(String showName, String dbName) {
        QueryColumn column = new QueryColumn();
        column.setTable(new QueryTable("订单", "orders"));
        column.setShowName(showName);
        column.setDbName(dbName);
        column.setDataType(PropertyType.Long);
        return column;
    }
}
