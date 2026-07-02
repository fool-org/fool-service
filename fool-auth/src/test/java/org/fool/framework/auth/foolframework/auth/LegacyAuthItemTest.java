package org.fool.framework.auth.foolframework.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LegacyAuthItemTest {
    @Test
    public void gettersKeepLegacyNotImplementedSurface() {
        AuthItem item = new AuthItem();

        assertNotImplemented(item::getID);
        assertNotImplemented(item::getType);
    }

    @Test
    public void settersAreLegacyNoOps() {
        AuthItem item = new AuthItem();

        item.setID(1);
        item.setType(2);
    }

    private static void assertNotImplemented(UnsupportedGetter getter) {
        try {
            getter.get();
            fail("expected legacy NotImplementedException surface");
        } catch (UnsupportedOperationException ex) {
            assertEquals("NotImplementedException", ex.getMessage());
        }
    }

    private interface UnsupportedGetter {
        int get();
    }
}
