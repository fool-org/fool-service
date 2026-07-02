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

public class LegacyAuthorizedUserMappingTest {
    @Test
    public void mapsAuthorizedUserToLegacyAuthUserTable() throws Exception {
        Table table = AuthorizedUser.class.getDeclaredAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("SW_APP_AUTH_USER", table.value());
        assertEquals("APP_AUTH_", table.columnPrefix());

        Column[] userColumns = field("user").getDeclaredAnnotationsByType(Column.class);
        assertEquals(2, userColumns.length);
        assertEquals("APP_AUTH_USERID", userColumns[0].value());
        assertEquals("UserID", userColumns[0].propertyName());
        assertEquals("APP_AUTH_USERLOGINNAME", userColumns[1].value());
        assertEquals("LoginName", userColumns[1].propertyName());
        assertEquals(User.class, field("user").getType());

        assertColumn("department", "APP_AUTH_DEP", false);
        assertEquals(Department.class, field("department").getType());
        assertColumn("authorizedId", "APP_AUTH_ID", true);
    }

    @Test
    public void carriesLegacyAuthorizedIdKeyGenerationMetadata() throws Exception {
        Column column = field("authorizedId").getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(true, column.key());
        assertEquals(GenerationType.ON_INSERT, column.generationType());
        assertEquals(
                SqlGenerateConfig.AUTO_INCREMENT,
                field("authorizedId").getDeclaredAnnotation(SqlGenerate.class).value());
    }

    private static void assertColumn(String fieldName, String columnName, boolean id) throws Exception {
        Field field = field(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertNotNull(column);
        assertEquals(columnName, column.value());
        assertEquals(id, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static Field field(String fieldName) throws Exception {
        return AuthorizedUser.class.getDeclaredField(fieldName);
    }
}
