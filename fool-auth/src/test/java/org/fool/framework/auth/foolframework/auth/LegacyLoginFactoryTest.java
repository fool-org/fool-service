package org.fool.framework.auth.foolframework.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyLoginFactoryTest {
    @Test
    public void matchesLegacyUnicodeMd5WithoutBytePadding() {
        assertEquals("19a2854144b63a8f7617a6f22519b12", LoginFactory.toMD5("admin"));
        assertEquals("b081dbe85e1ec3ffc3d4e7d022740cd", LoginFactory.toMD5("password"));
        assertEquals("ce1473cf80c6b3fda8e3dfc06adc315", LoginFactory.toMD5("abc"));
        assertEquals("d41d8cd98f0b24e980998ecf8427e", LoginFactory.toMD5(""));
    }
}
