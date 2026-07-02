package org.fool.framework.dao;

import org.fool.framework.common.annotation.Table;
import org.junit.Test;

import java.sql.ResultSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqlScriptGeneratorMigrationTest {

    @Test
    public void generateInsertUsesEnumOrdinalsForSqlArguments() {
        EnumRecord record = new EnumRecord();
        record.name = "order";
        record.state = RecordState.ACTIVE;

        QueryAndArgs queryAndArgs = new SqlScriptGenerator().generateOnInsert(new Mapper<>(EnumRecord.class), record);

        assertArrayEquals(new Object[]{"order", 1}, queryAndArgs.getArgs());
    }

    @Test
    public void generateInsertUsesLegacyEnumCodeWhenAvailable() {
        CodeRecord record = new CodeRecord();
        record.name = "order";
        record.state = CodeState.SECOND;

        QueryAndArgs queryAndArgs = new SqlScriptGenerator().generateOnInsert(new Mapper<>(CodeRecord.class), record);

        assertArrayEquals(new Object[]{"order", 20}, queryAndArgs.getArgs());
    }

    @Test
    public void mapRowUsesLegacyEnumCodeWhenAvailable() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("name")).thenReturn("order");
        when(resultSet.getInt("state")).thenReturn(20);

        CodeRecord record = new Mapper<>(CodeRecord.class).mapRow(resultSet, 0);

        assertEquals("order", record.name);
        assertEquals(CodeState.SECOND, record.state);
    }

    @Table("enum_record")
    private static class EnumRecord {
        private String name;
        private RecordState state;
    }

    private enum RecordState {
        NEW,
        ACTIVE
    }

    @Table("code_record")
    static class CodeRecord {
        private String name;
        private CodeState state;
    }

    private enum CodeState {
        FIRST(10),
        SECOND(20);

        private final int code;

        CodeState(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}
