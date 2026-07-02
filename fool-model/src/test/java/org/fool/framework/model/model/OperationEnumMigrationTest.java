package org.fool.framework.model.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperationEnumMigrationTest {
    @Test
    public void operationTypeKeepsLegacyCodesAndSpelling() {
        assertEquals(0, OperationType.DANYMIC.code());
        assertEquals(1, OperationType.REFLECTION.code());
    }

    @Test
    public void commandsTypeKeepsLegacyCodesAndSpelling() {
        assertEquals(0, CommandsType.SET_VALUE.code());
        assertEquals(1, CommandsType.SET_ACCESS.code());
        assertEquals(2, CommandsType.EXUTE_PROPRTY_MODEL_METHOD.code());
        assertEquals(3, CommandsType.EXUTE_OUT_MODEL_METHOD.code());
        assertEquals(4, CommandsType.SET_SOURCE.code());
        assertEquals(5, CommandsType.EXUTE_LIST_METHOD.code());
        assertEquals(6, CommandsType.FILTER.code());
        assertEquals(7, CommandsType.SET_PARAM_VALUE.code());
        assertEquals(8, CommandsType.SET_CON_STR_VALUE.code());
    }

    @Test
    public void orderByTypeKeepsLegacyCodes() {
        assertEquals(0, OrderByType.ASC.code());
        assertEquals(1, OrderByType.DESC.code());
    }
}
