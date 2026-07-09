package org.fool.framework.app;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class AppInstalledModelLegacySchemaTest {
    @Test
    public void mapsFullLegacyModelSchemaColumns() throws Exception {
        assertEquals("SW_SYS_MODEL", tableName(AppInstalledModel.class));
        assertColumn("baseModelId", "MODEL_PARENT", false);
        assertColumn("idPropertyId", "MODEL_IDPROPERTY", false);
        assertColumn("defaultFormat", "MODEL_DEFAULTFORMAT", false);
        assertColumn("modelType", "MODEL_TYPE", false);
        assertColumn("view", "MODEL_ISVIEW", false);
        assertColumn("defaultListViewId", "MODEL_DEFAULTLISTVIEW", false);
        assertColumn("defaultItemViewId", "MODEL_DEFAULTITEMVIEW", false);
    }

    private static void assertColumn(String fieldName, String columnName, boolean key) throws Exception {
        Field field = AppInstalledModel.class.getDeclaredField(fieldName);
        Column column = field.getDeclaredAnnotation(Column.class);
        assertEquals(columnName, column.value());
        assertEquals(key, field.getDeclaredAnnotation(Id.class) != null);
    }

    private static String tableName(Class<?> type) {
        return type.getDeclaredAnnotation(Table.class).value();
    }
}
