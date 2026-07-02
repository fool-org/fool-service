package org.fool.framework.common.data;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BusinessObjectTest {
    @Test
    public void businessObjectKeepsLegacyBoIdColumnSurface() throws Exception {
        Order order = new Order();
        order.setId(42L);

        Field id = BusinessObject.class.getDeclaredField("id");
        Column column = id.getDeclaredAnnotation(Column.class);

        assertEquals(42L, order.getId());
        assertEquals("BO_Id", id.getDeclaredAnnotation(Id.class).value());
        assertEquals("BO_Id", column.value());
        assertTrue(column.key());
        assertEquals(GenerationType.ON_INSERT, column.generationType());
        assertTrue(order instanceof IBusinessObject);
    }

    private static class Order extends BusinessObject {
    }
}
