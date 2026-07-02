package org.fool.framework.common.annotation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ColumnTest {
    @Test
    public void exposesLegacyColumnExtraMetadataAtRuntime() throws Exception {
        Column column = Sample.class.getDeclaredField("id").getAnnotation(Column.class);

        assertEquals("ID", column.value());
        assertTrue(column.keyCanBeNullOrEmpty());
        assertEquals("BigInt", column.sqlType());
        assertTrue(column.identify());
    }

    @Test
    public void defaultsLegacyColumnExtraMetadata() throws Exception {
        Column column = Sample.class.getDeclaredField("name").getAnnotation(Column.class);

        assertFalse(column.keyCanBeNullOrEmpty());
        assertEquals("", column.sqlType());
        assertFalse(column.identify());
    }

    @Test
    public void encryptTypesKeepLegacyCodes() {
        assertEquals(0, EncryptType.NONE.code());
        assertEquals(1, EncryptType.MD5.code());
        assertEquals(2, EncryptType.RADOM_DECS.code());
    }

    private static class Sample {
        @Column(value = "ID", keyCanBeNullOrEmpty = true, sqlType = "BigInt", identify = true)
        private long id;

        @Column("NAME")
        private String name;
    }
}
