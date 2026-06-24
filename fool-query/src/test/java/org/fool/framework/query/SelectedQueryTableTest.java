package org.fool.framework.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SelectedQueryTableTest {
    @Test
    public void selectedQueryTableKeepsLegacyUnsupportedGetterAndNoopSetter() {
        SelectedQueryTable selectedQueryTable = new SelectedQueryTable();
        SelectedTable table = new SelectedTable(new QueryTable("Orders", "orders"), "orders");

        selectedQueryTable.setTable(table);

        try {
            selectedQueryTable.getTable();
            fail("expected getter to preserve legacy NotImplemented surface");
        } catch (UnsupportedOperationException ex) {
            assertEquals("NotImplementedException", ex.getMessage());
        }
    }
}
