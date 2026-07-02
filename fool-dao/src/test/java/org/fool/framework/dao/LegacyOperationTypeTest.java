package org.fool.framework.dao;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class LegacyOperationTypeTest {
    @Test
    public void operationTypeKeepsLegacyOrdinals() {
        assertEquals(0, OperationType.CREATE.code());
        assertEquals(1, OperationType.SAVE.code());
        assertEquals(2, OperationType.DELETE.code());
        assertEquals(3, OperationType.DYNAMIC_UPDATE.code());
    }

    @Test
    public void sqlOperationKeepsLegacyQueueOperationNames() {
        assertEquals("insert", SqlOperation.INSERT.legacyName());
        assertEquals("updateafterinsert", SqlOperation.UPDATE_AFTER_INSERT.legacyName());
        assertEquals("update", SqlOperation.UPDATE.legacyName());
        assertEquals("updateafterupdate", SqlOperation.UPDATE_AFTER_UPDATE.legacyName());
        assertEquals("delete", SqlOperation.DELETE.legacyName());
        assertEquals("excute", SqlOperation.EXCUTE.legacyName());
    }

    @Test
    public void sqlTransAutoMicCarriesCommandObjectAndOperation() {
        Object command = new Object();
        Object target = new Object();

        SqlTransAutoMic item = new SqlTransAutoMic(command, target, SqlOperation.UPDATE);

        assertSame(command, item.getCommand());
        assertSame(target, item.getObject());
        assertEquals(SqlOperation.UPDATE, item.getOperation());
    }
}
