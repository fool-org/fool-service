package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QueryLookupCollectionTest {

    @Test
    public void tableCollectionFindsByDbNameOrShowNameIgnoringCaseAndWhitespace() {
        QueryTable orders = new QueryTable("订单", "orders");
        QueryTable accounts = new QueryTable("账户", "accounts");

        QueryTableCollection tables = new QueryTableCollection();
        tables.add(orders);
        tables.add(accounts);

        assertEquals(orders, tables.find(" ORDERS "));
        assertEquals(accounts, tables.find(" 账户 "));
        assertNull(tables.find("missing"));
    }

    @Test
    public void columnCollectionFindsByColumnNameOrQualifiedTableColumnName() {
        QueryTable orders = new QueryTable("订单", "orders");
        QueryColumn orderId = column(orders, "订单号", "ORDER_ID");
        QueryColumn symbol = column(orders, "交易对", "SYMBOL");

        QueryColumnCollection columns = new QueryColumnCollection();
        columns.add(orderId);
        columns.add(symbol);

        assertEquals(orderId, columns.find(" order_id "));
        assertEquals(symbol, columns.find("交易对"));
        assertEquals(orderId, columns.find("orders.order_id"));
        assertEquals(symbol, columns.find("订单.交易对"));
        assertNull(columns.find("orders.missing"));
    }

    private QueryColumn column(QueryTable table, String showName, String dbName) {
        QueryColumn column = new QueryColumn();
        column.setTable(table);
        column.setShowName(showName);
        column.setDbName(dbName);
        column.setDataType(PropertyType.String);
        return column;
    }
}
