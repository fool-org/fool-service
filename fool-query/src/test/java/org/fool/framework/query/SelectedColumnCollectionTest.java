package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void insertKeepsLegacyDirectListInsertSurface() throws Exception {
        SelectedColumnCollection columns = new SelectedColumnCollection();
        SelectedColumn first = new SelectedColumn("orderId", queryColumn("ORDER_ID"));
        SelectedColumn inserted = new SelectedColumn("symbol", queryColumn("SYMBOL"));
        inserted.setSelectedIndex(99);
        columns.add(first);

        method("insert", int.class, SelectedColumn.class).invoke(columns, 0, inserted);

        assertEquals(inserted, columns.get(0));
        assertEquals(first, columns.get(1));
        assertEquals(99, inserted.getSelectedIndex());
        assertEquals(0, first.getSelectedIndex());
    }

    @Test
    public void removeAtKeepsLegacyDirectListRemovalSurface() throws Exception {
        SelectedColumnCollection columns = new SelectedColumnCollection();
        SelectedColumn first = new SelectedColumn("orderId", queryColumn("ORDER_ID"));
        SelectedColumn second = new SelectedColumn("symbol", queryColumn("SYMBOL"));
        columns.add(first);
        columns.add(second);

        method("removeAt", int.class).invoke(columns, 0);

        assertEquals(1, columns.size());
        assertEquals(second, columns.get(0));
        assertEquals(1, second.getSelectedIndex());
    }

    @Test
    public void isReadOnlyKeepsLegacyCollectionFlag() {
        SelectedColumnCollection columns = new SelectedColumnCollection();

        assertTrue(columns.isReadOnly());
    }

    @Test
    public void indexedSetterKeepsLegacyNotImplementedSurface() {
        SelectedColumnCollection columns = new SelectedColumnCollection();
        columns.add(new SelectedColumn("orderId", queryColumn("ORDER_ID")));

        try {
            columns.set(0, new SelectedColumn("symbol", queryColumn("SYMBOL")));
            fail("expected indexed setter to preserve legacy NotImplemented surface");
        } catch (UnsupportedOperationException ex) {
            assertEquals("NotImplementedException", ex.getMessage());
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

    private Method method(String name, Class<?>... parameterTypes) {
        try {
            return SelectedColumnCollection.class.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ex) {
            fail("SelectedColumnCollection should expose legacy " + name + " method");
            return null;
        }
    }
}
