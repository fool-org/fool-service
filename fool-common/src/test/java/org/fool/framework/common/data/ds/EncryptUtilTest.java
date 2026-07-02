package org.fool.framework.common.data.ds;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptUtilTest {
    @Test
    public void matchesLegacyUnicodeMd5WithoutBytePadding() {
        assertEquals("19a2854144b63a8f7617a6f22519b12", EncryptUtil.toMD5("admin"));
        assertEquals("b081dbe85e1ec3ffc3d4e7d022740cd", EncryptUtil.toMD5("password"));
        assertEquals("ce1473cf80c6b3fda8e3dfc06adc315", EncryptUtil.toMD5("abc"));
        assertEquals("d41d8cd98f0b24e980998ecf8427e", EncryptUtil.toMD5(""));
    }
}
