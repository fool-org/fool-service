package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SelectedColumnCollectionTest {

    @Test
    public void addReindexesSelectedColumnsByInsertionOrder() {
        SelectedColumnCollection columns = new SelectedColumnCollection();

        SelectedColumn first = new SelectedColumn("orderId", queryColumn("ORDER_ID"));
        SelectedColumn second = new SelectedColumn("symbol", queryColumn("SYMBOL"));

        columns.add(first);
        columns.add(second);

        assertEquals(0, first.getSelectedIndex());
        assertEquals(1, second.getSelectedIndex());
        assertEquals(first, columns.get(0));
        assertEquals(second, columns.get(1));
    }

    @Test
    public void addRejectsDuplicateSelectedNameLikeLegacyCollection() {
        SelectedColumnCollection columns = new SelectedColumnCollection();
        columns.add(new SelectedColumn("symbol", queryColumn("SYMBOL")));

        try {
            columns.add(new SelectedColumn("symbol", queryColumn("SYMBOL2")));
            fail("expected duplicate selected name to be rejected");
        } catch (IllegalArgumentException ex) {
            assertEquals("已经有相同的列名称存在", ex.getMessage());
        }
    }

    private QueryColumn queryColumn(String dbName) {
        QueryTable table = new QueryTable("Orders", "orders");
        QueryColumn column = new QueryColumn();
        column.setTable(table);
        column.setDbName(dbName);
        column.setShowName(dbName);
        column.setDataType(PropertyType.String);
        return column;
    }
}
