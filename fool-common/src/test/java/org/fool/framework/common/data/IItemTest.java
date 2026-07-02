package org.fool.framework.common.data;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class IItemTest {
    @Test
    public void setParentCastsLegacyObjectParentToTypedParent() {
        Order parent = new Order();
        OrderLine line = new OrderLine();

        line.setParent(parent);

        assertSame(parent, line.getParent());
        assertTrue(line instanceof IBusinessObject);
    }

    private static class Order {
    }

    private static class OrderLine extends IItem<Order> {
        private Order parent;

        @Override
        public Order getParent() {
            return parent;
        }

        @Override
        protected void setTypedParent(Order parent) {
            this.parent = parent;
        }
    }
}
