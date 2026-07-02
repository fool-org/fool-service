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

public class LegacyAuthRoleMappingTest {
    @Test
    public void mapsRoleToLegacyAuthRoleTable() throws Exception {
        Table table = Role.class.getDeclaredAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("SW_APP_AUTH_ROLE", table.value());
        assertEquals("AUTH_ROLE_", table.columnPrefix());

        assertColumn("roleId", "AUTH_ROLE_ID", true);
        assertColumn("roleName", "AUTH_ROLE_NAME", false);
    }

    @Test
    public void carriesLegacyRoleKeyGenerationMetadata() throws Exception {
        Column column = field("roleId").getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(true, column.key());
        assertEquals(GenerationType.ON_INSERT, column.generationType());
        assertEquals(
                SqlGenerateConfig.AUTO_INCREMENT,
                field("roleId").getDeclaredAnnotation(SqlGenerate.class).value());
    }

    private static void assertColumn(String fieldName, String columnName, boolean id) throws Exception {
        Field field = field(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.value());
        assertEquals(id, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static Field field(String fieldName) throws Exception {
        return Role.class.getDeclaredField(fieldName);
    }
}
