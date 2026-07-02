package org.fool.framework.common.data;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class BusinesObjectsWithItemTest {
    @Test
    public void storesLegacySubItemListProperty() {
        BusinesObjectsWithItem<String> owner = new BusinesObjectsWithItem<>();
        SubItemList<String> list = new SubItemList<>();

        assertNull(owner.getList());

        owner.setList(list);

        assertSame(list, owner.getList());
    }
}
