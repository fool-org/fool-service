package org.fool.framework.common.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerialNoObjectTest {
    @Test
    public void exposesLegacySerialNoLenProperty() {
        Invoice invoice = new Invoice();

        invoice.setSerialNoLen(12);

        assertEquals(12, invoice.getSerialNoLen());
    }

    private static class Invoice implements SerialNoObject {
        private int serialNoLen;

        @Override
        public int getSerialNoLen() {
            return serialNoLen;
        }

        @Override
        public void setSerialNoLen(int serialNoLen) {
            this.serialNoLen = serialNoLen;
        }
    }
}
