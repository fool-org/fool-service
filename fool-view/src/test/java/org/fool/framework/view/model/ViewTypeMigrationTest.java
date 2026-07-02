package org.fool.framework.view.model;

import org.fool.framework.common.annotation.Table;
import org.fool.framework.dao.Mapper;
import org.junit.Test;

import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ViewTypeMigrationTest {
    @Test
    public void viewTypeKeepsLegacyCodes() {
        assertEquals(0, ViewType.ListView.code());
        assertEquals(1, ViewType.DetailView.code());
        assertEquals(2, ViewType.QueryView.code());
        assertEquals(3, ViewType.MapView.code());
        assertEquals(4, ViewType.GroupView.code());
        assertEquals(5, ViewType.ReportView.code());
        assertEquals(6, ViewType.ButtonListView.code());
        assertEquals(7, ViewType.GridView.code());
    }

    @Test
    public void mapperReadsLegacyViewTypeCodes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("name")).thenReturn("order");
        when(resultSet.getInt("view_type")).thenReturn(1);

        ViewRecord record = new Mapper<>(ViewRecord.class).mapRow(resultSet, 0);

        assertEquals("order", record.name);
        assertEquals(ViewType.DetailView, record.viewType);
    }

    @Table("view_record")
    public static class ViewRecord {
        private String name;
        private ViewType viewType;
    }
}
