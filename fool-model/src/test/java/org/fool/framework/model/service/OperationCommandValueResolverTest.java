package org.fool.framework.model.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperationCommandValueResolverTest {
    @Test
    public void resolvesLegacyContextValuesThroughCallback() {
        Object value = new OperationCommandValueResolver().resolve(
                null,
                null,
                "@userid",
                (property, raw) -> raw,
                key -> "userid".equals(key) ? "admin" : "");

        assertEquals("admin", value);
    }
}
