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
    public void itemEditTypeKeepsLegacyWebMapCodes() {
        assertEquals(16, ItemEditType.MapLongitude.code());
        assertEquals(17, ItemEditType.MapLatitude.code());
        assertEquals(18, ItemEditType.MapTitle.code());
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

    @Test
    public void mapperReadsLegacyWebMapEditTypeCodes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("name")).thenReturn("longitude");
        when(resultSet.getInt("edit_type")).thenReturn(16);

        ViewItemRecord record = new Mapper<>(ViewItemRecord.class).mapRow(resultSet, 0);

        assertEquals("longitude", record.name);
        assertEquals(ItemEditType.MapLongitude, record.editType);
    }

    @Table("view_record")
    public static class ViewRecord {
        private String name;
        private ViewType viewType;
    }

    @Table("view_item_record")
    public static class ViewItemRecord {
        private String name;
        private ItemEditType editType;
    }
}
