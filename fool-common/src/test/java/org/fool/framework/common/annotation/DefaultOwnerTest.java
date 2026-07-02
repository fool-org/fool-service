package org.fool.framework.common.annotation;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DefaultOwnerTest {
    @Test
    public void exposesLegacyDefaultOwnerMarkerAtRuntime() throws Exception {
        DefaultOwner owner = Sample.class.getDeclaredField("owner").getAnnotation(DefaultOwner.class);

        assertNotNull(owner);
    }

    private static class Sample {
        @DefaultOwner
        private String owner;
    }
}
