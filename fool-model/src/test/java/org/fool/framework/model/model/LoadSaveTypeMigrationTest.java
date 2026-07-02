package org.fool.framework.model.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoadSaveTypeMigrationTest {
    @Test
    public void loadTypeKeepsLegacyCodes() {
        assertEquals(0, LoadType.NULL.code());
        assertEquals(1, LoadType.PARTIAL.code());
        assertEquals(2, LoadType.COMPLETE.code());
        assertEquals(3, LoadType.NO_OBJ.code());
    }

    @Test
    public void saveTypeKeepsLegacyCodes() {
        assertEquals(0, SaveType.UNKNOWN.code());
        assertEquals(1, SaveType.EXISTS.code());
        assertEquals(2, SaveType.UN_EXISTS.code());
    }
}
