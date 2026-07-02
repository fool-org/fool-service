package org.fool.framework.common.annotation;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class DynamicColumnTest {
    @Test
    public void exposesLegacyDynamicColumnMetadataAtRuntime() throws Exception {
        Field sum = Sample.class.getDeclaredField("sum");
        DynamicColumn column = sum.getAnnotation(DynamicColumn.class);

        assertEquals("add", column.sourcePropertyName());
        assertEquals(DynamicOperationType.SUB, column.operation());
        assertEquals(1, DynamicOperationType.ADD.code());
        assertEquals(2, DynamicOperationType.SUB.code());
    }

    @Test
    public void defaultsToLegacyAddOperation() throws Exception {
        DynamicColumn column = Sample.class.getDeclaredField("defaulted").getAnnotation(DynamicColumn.class);

        assertEquals("", column.sourcePropertyName());
        assertEquals(DynamicOperationType.ADD, column.operation());
    }

    private static class Sample {
        @DynamicColumn(sourcePropertyName = "add", operation = DynamicOperationType.SUB)
        private int sum;

        @DynamicColumn
        private int defaulted;
    }
}
