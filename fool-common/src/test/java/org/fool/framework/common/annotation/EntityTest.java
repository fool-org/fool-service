package org.fool.framework.common.annotation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityTest {
    @Test
    public void exposesLegacyEntityNameAtRuntime() {
        Entity entity = Named.class.getAnnotation(Entity.class);

        assertEquals("Order", entity.name());
    }

    @Test
    public void entityNameDefaultsToEmptyString() {
        Entity entity = Defaulted.class.getAnnotation(Entity.class);

        assertEquals("", entity.name());
    }

    @Entity(name = "Order")
    private static class Named {
    }

    @Entity
    private static class Defaulted {
    }
}
