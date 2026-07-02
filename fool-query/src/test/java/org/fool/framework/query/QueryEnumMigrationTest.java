package org.fool.framework.query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QueryEnumMigrationTest {
    @Test
    public void orderTypeKeepsLegacyCodes() {
        assertEquals(0, OrderType.ASC.code());
        assertEquals(1, OrderType.DESC.code());
        assertEquals(2, OrderType.NULL.code());
    }

    @Test
    public void addQueryTableKeepsLegacyCodes() {
        assertEquals(0, AddQueryTable.Success.code());
        assertEquals(1, AddQueryTable.NoRelation.code());
        assertEquals(2, AddQueryTable.Exists.code());
    }

    @Test
    public void joinQueryTypeKeepsLegacyCodes() {
        assertEquals(0, JoinQueryType.Parent.code());
        assertEquals(1, JoinQueryType.Items.code());
        assertEquals(2, JoinQueryType.All.code());
    }
}
