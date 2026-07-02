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

public class ModelTypeMigrationTest {
    @Test
    public void modelTypeKeepsLegacyCodes() {
        assertEquals(0, ModelType.DYNAMIC.code());
        assertEquals(1, ModelType.ABSTRACT_CLASS.code());
        assertEquals(2, ModelType.ENUM.code());
    }

    @Test
    public void daoReadsLegacyModelTypeCodes() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("name")).thenReturn("state");
        when(resultSet.getInt("model_type")).thenReturn(2);

        ModelRecord record = new Mapper<>(ModelRecord.class).mapRow(resultSet, 0);

        assertEquals("state", record.name);
        assertEquals(ModelType.ENUM, record.modelType);
    }

    @Test
    public void daoWritesLegacyModelTypeCodes() {
        ModelRecord record = new ModelRecord();
        record.name = "order";
        record.modelType = ModelType.DYNAMIC;

        QueryAndArgs queryAndArgs = new SqlScriptGenerator().generateOnInsert(new Mapper<>(ModelRecord.class), record);

        assertArrayEquals(new Object[]{"order", 0}, queryAndArgs.getArgs());
    }

    @Table("model_record")
    public static class ModelRecord {
        private String name;
        private ModelType modelType;
    }
}
