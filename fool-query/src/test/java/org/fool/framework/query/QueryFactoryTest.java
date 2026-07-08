package org.fool.framework.query;

import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class QueryFactoryTest {
    @Test
    public void queryFactoryFindsTablesByLegacyDbNameOnlyAndKeepsEmptyColumnDefault() {
        QueryTable orders = new QueryTable("订单", "orders");
        QueryTable customers = new QueryTable("客户", "customers");
        QueryFactory factory = new QueryFactory() {
            @Override
            public List<JoinTable> getCanJoinedTables(QueryTable table, JoinQueryType joinType) {
                return List.of();
            }

            @Override
            public List<QueryTable> getTables() {
                return List.of(orders, customers);
            }
        };

        assertEquals(orders, factory.getTable("orders"));
        assertEquals(orders, factory.getTable(" ORDERS "));
        assertEquals(customers, factory.getTable("customers"));
        assertThrows(NoSuchElementException.class, () -> factory.getTable("订单"));
        assertThrows(NoSuchElementException.class, () -> factory.getTable(" 客户 "));
        assertThrows(NoSuchElementException.class, () -> factory.getTable("missing"));
        assertEquals(List.of(), factory.getColumns(orders));
    }

    @Test
    public void getStateStrMapsLegacyDbValueToShowNameOnly() {
        QueryColumn column = new QueryColumn();
        column.setShowName("状态");
        column.setDbName("STATUS");
        QueryFactory factory = new QueryFactory() {
            @Override
            public List<JoinTable> getCanJoinedTables(QueryTable table, JoinQueryType joinType) {
                return List.of();
            }

            @Override
            public List<ColStateValue> getStateValues(QueryColumn col) {
                assertEquals(column, col);
                ColStateValue ready = stateValue("就绪", "READY");
                ColStateValue done = stateValue("完成", "DONE");
                return List.of(ready, done);
            }
        };

        assertEquals("就绪", factory.getStateStr(column, "READY"));
        assertEquals("完成", factory.getStateStr(column, "DONE"));
        assertEquals("", factory.getStateStr(column, "就绪"));
        assertEquals("", factory.getStateStr(column, "UNKNOWN"));
    }

    private static ColStateValue stateValue(String showName, String dbName) {
        ColStateValue value = new ColStateValue();
        value.setShowName(showName);
        value.setDbName(dbName);
        return value;
    }
}
