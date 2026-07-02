package org.fool.framework.common.data;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class IObjectFromDataBaseTest {
    @Test
    public void exposesLegacyRowProperty() {
        RowBackedObject object = new RowBackedObject();
        Map<String, Object> row = Map.of("ID", 1001L);

        object.setRow(row);

        assertEquals(row, object.getRow());
    }

    private static class RowBackedObject implements IObjectFromDataBase {
        private Map<String, Object> row;

        @Override
        public Map<String, Object> getRow() {
            return row;
        }

        @Override
        public void setRow(Map<String, Object> row) {
            this.row = row;
        }
    }
}
