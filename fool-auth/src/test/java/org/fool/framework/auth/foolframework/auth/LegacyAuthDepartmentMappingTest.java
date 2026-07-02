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

public class LegacyAuthDepartmentMappingTest {
    @Test
    public void mapsDepartmentToLegacyAuthDepartmentTable() throws Exception {
        Table table = Department.class.getDeclaredAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("SW_APP_AUTH_DEPARTMENT", table.value());
        assertEquals("APP_DEP_", table.columnPrefix());

        assertColumn("depId", "APP_DEP_ID", true);
        assertColumn("departmentName", "APP_DEP_NAME", false);
        assertColumn("defaultView", "APP_DEP_DEFAULTVIEW", false);
    }

    @Test
    public void carriesLegacyDepartmentKeyGenerationMetadata() throws Exception {
        Column column = field("depId").getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(true, column.key());
        assertEquals(GenerationType.ON_INSERT, column.generationType());
        assertEquals(
                SqlGenerateConfig.AUTO_INCREMENT,
                field("depId").getDeclaredAnnotation(SqlGenerate.class).value());
    }

    private static void assertColumn(String fieldName, String columnName, boolean id) throws Exception {
        Field field = field(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.value());
        assertEquals(id, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static Field field(String fieldName) throws Exception {
        return Department.class.getDeclaredField(fieldName);
    }
}
