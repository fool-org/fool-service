package org.fool.framework.model.model;

import org.fool.framework.common.annotation.Table;
import org.fool.framework.dao.Mapper;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.dao.SqlScriptGenerator;
import org.junit.Test;

import java.sql.ResultSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelationTypeMigrationTest {
    @Test
    public void relationTypeKeepsLegacyCodes() {
        assertEquals(0, RelationType.One2Many.code());
        assertEquals(1, RelationType.Many2Many.code());
        assertEquals(2, RelationType.Many2One.code());
        assertEquals(3, RelationType.Recurve.code());
    }

    @Test
    public void daoReadsLegacyRelationTypeCodes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("name")).thenReturn("items");
        when(resultSet.getInt("relation_type")).thenReturn(3);

        RelationRecord record = new Mapper<>(RelationRecord.class).mapRow(resultSet, 0);

        assertEquals("items", record.name);
        assertEquals(RelationType.Recurve, record.relationType);
    }

    @Test
    public void daoWritesLegacyRelationTypeCodes() {
        RelationRecord record = new RelationRecord();
        record.name = "items";
        record.relationType = RelationType.Many2Many;

        QueryAndArgs queryAndArgs = new SqlScriptGenerator().generateOnInsert(new Mapper<>(RelationRecord.class), record);

        assertArrayEquals(new Object[]{"items", 1}, queryAndArgs.getArgs());
    }

    @Table("relation_record")
    public static class RelationRecord {
        private String name;
        private RelationType relationType;
    }
}
