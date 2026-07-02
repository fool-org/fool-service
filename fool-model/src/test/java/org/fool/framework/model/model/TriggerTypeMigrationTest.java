package org.fool.framework.model.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TriggerTypeMigrationTest {
    @Test
    public void modelTriggerTypeKeepsLegacyCodes() {
        assertEquals(0, ModelTriggerType.CREATE.code());
        assertEquals(1, ModelTriggerType.SAVE.code());
        assertEquals(2, ModelTriggerType.DELETE.code());
        assertEquals(3, ModelTriggerType.BEFORE_CREATE.code());
        assertEquals(4, ModelTriggerType.BEFORE_SAVE.code());
        assertEquals(5, ModelTriggerType.BEFORE_DELETE.code());
    }

    @Test
    public void propertyTriggerTypeKeepsLegacyCodes() {
        assertEquals(0, PropertyTriggerType.SET.code());
        assertEquals(1, PropertyTriggerType.ITEMS_ADD.code());
        assertEquals(2, PropertyTriggerType.ITEMS_SET.code());
        assertEquals(3, PropertyTriggerType.ITEMS_DELETE.code());
    }
}
