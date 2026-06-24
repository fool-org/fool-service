package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryReportTest {

    @Test
    public void queryReportDefinitionStoresLegacyReportContract() {
        QueryColumn orderId = column("订单号", "ORDER_ID");
        QueryParameter orderParam = new QueryParameter("orderId", new CompareCol(orderId, "Orders"), 1001L);

        QueryReportDefinition definition = new QueryReportDefinition("SELECT [ORDER_ID] FROM [orders]");
        definition.setReportName("Order Query");
        definition.setReportNo("QR-001");
        definition.getColumns().add(orderId);
        definition.getParameters().add(orderParam);

        QueryReport report = definition;

        assertEquals("SELECT [ORDER_ID] FROM [orders]", report.getSqlStript());
        assertEquals("Order Query", report.getReportName());
        assertEquals("QR-001", report.getReportNo());
        assertEquals(orderId, report.getColumns().get(0));
        assertEquals("orderId", report.getParameters().get(0).getName());
        assertEquals(1001L, report.getParameters().get(0).getValue());
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
