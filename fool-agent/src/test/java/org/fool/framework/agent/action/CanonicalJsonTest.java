package org.fool.framework.agent.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CanonicalJsonTest {
    private final CanonicalJson canonical = new CanonicalJson(new ObjectMapper());

    @Test
    public void hashIsStableAcrossObjectFieldOrder() {
        assertEquals(
                canonical.hashJson("{\"b\":2,\"nested\":{\"z\":1,\"a\":2},\"a\":1}"),
                canonical.hashJson("{\"a\":1,\"nested\":{\"a\":2,\"z\":1},\"b\":2}"));
    }
}
