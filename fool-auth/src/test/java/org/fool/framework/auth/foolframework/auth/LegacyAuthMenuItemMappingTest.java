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

public class LegacyAuthMenuItemMappingTest {
    @Test
    public void mapsMenuItemToLegacyAuthMenuTable() throws Exception {
        Table table = MenuItem.class.getDeclaredAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("SW_APP_AUTH_MENU", table.value());
        assertEquals("AUTH_MENU_", table.columnPrefix());

        assertColumn("id", "AUTH_MENU_ID", true);
        assertColumn("text", "AUTH_MENU_TEXT", false);
        assertColumn("shortcutKey", "AUTH_MENU_SHORTCUTKEY", false);
        assertColumn("image", "AUTH_MENU_IMAGE", false);
        assertColumn("defaultVisible", "AUTH_MENU_VISIABLE", false);
        assertColumn("defaultEnable", "AUTH_MENU_ENABLE", false);
        assertColumn("viewId", "AUTH_MENU_VIEWID", false);
        assertColumn("templateFile", "AUTH_MENU_TEMPLATEFILE", false);
        assertColumn("index", "AUTH_MENU_INDEX", false);
    }

    @Test
    public void carriesLegacyMenuItemKeyGenerationMetadata() throws Exception {
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
        return MenuItem.class.getDeclaredField(fieldName);
    }
}
