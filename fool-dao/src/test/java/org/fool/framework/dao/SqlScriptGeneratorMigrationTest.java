package org.fool.framework.dao;

import org.fool.framework.common.annotation.Table;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SqlScriptGeneratorMigrationTest {

    @Test
    public void generateInsertUsesEnumOrdinalsForSqlArguments() {
        EnumRecord record = new EnumRecord();
        record.name = "order";
        record.state = RecordState.ACTIVE;

        QueryAndArgs queryAndArgs = new SqlScriptGenerator().generateOnInsert(new Mapper<>(EnumRecord.class), record);

        assertArrayEquals(new Object[]{"order", 1}, queryAndArgs.getArgs());
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
}
