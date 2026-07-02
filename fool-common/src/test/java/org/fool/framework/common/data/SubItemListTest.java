package org.fool.framework.common.data;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubItemListTest {
    @Test
    public void tracksLegacyAddedUpdatedAndDeletedItems() {
        SubItemList<String> items = new SubItemList<>();

        items.add("new");
        items.set(0, "updated");
        items.remove("updated");

        assertEquals(List.of("new"), items.getAddedList());
        assertEquals(List.of("updated"), items.getUpdatedList());
        assertEquals(List.of("updated"), items.getDeleteList());
        assertFalse(items.remove("missing"));
    }

    @Test
    public void removesDeletedItemFromLegacyAddedList() {
        SubItemList<String> items = new SubItemList<>();

        items.add("temp");
        items.remove(0);

        assertEquals(List.of(), items.getAddedList());
        assertEquals(List.of("temp"), items.getDeleteList());
    }
}
