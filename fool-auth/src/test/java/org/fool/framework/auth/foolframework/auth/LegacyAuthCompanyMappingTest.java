package org.fool.framework.auth.foolframework.auth;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LegacyAuthCompanyMappingTest {
    @Test
    public void mapsCompanyToLegacyAuthCompanyTable() throws Exception {
        Table table = Company.class.getDeclaredAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("SW_APP_AUTH_COMPANY", table.value());
        assertEquals("APP_COR_", table.columnPrefix());

        assertColumn("id", "APP_COR_ID", true);
        assertColumn("name", "APP_COR_NAME", false);
    }

    @Test
    public void carriesLegacyCompanyKeyGenerationMetadata() throws Exception {
        Column column = field("id").getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(true, column.key());
        assertEquals(GenerationType.ON_INSERT, column.generationType());
        assertEquals(
                SqlGenerateConfig.AUTO_INCREMENT,
                field("id").getDeclaredAnnotation(SqlGenerate.class).value());
    }

    private static void assertColumn(String fieldName, String columnName, boolean id) throws Exception {
        Field field = field(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.value());
        assertEquals(id, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static Field field(String fieldName) throws Exception {
        return Company.class.getDeclaredField(fieldName);
    }
}
