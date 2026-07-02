package org.fool.framework.common.annotation;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class ParentRelationTest {
    @Test
    public void exposesLegacyParentRelationMetadataAtRuntime() throws Exception {
        Field parent = Sample.class.getDeclaredField("parent");
        ParentRelation relation = parent.getAnnotation(ParentRelation.class);

        assertEquals("order", relation.propertyName());
        assertEquals("ORDER_ID", relation.columnName());
    }

    private static class Sample {
        @ParentRelation(propertyName = "order", columnName = "ORDER_ID")
        private String parent;
    }
}
