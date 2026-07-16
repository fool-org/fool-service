package org.fool.framework.auth.security;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DepartmentTreeResolverTest {
    @Test
    public void expandsDescendantsOnceAndStopsOnCycles() {
        List<String> result = DepartmentTreeResolver.expand(List.of("10"), List.of(
                Map.of("PARENT_ID", "10", "CHILD_ID", "11"),
                Map.of("PARENT_ID", "11", "CHILD_ID", "12"),
                Map.of("PARENT_ID", "12", "CHILD_ID", "10"),
                Map.of("PARENT_ID", "99", "CHILD_ID", "100")));

        assertEquals(List.of("10", "11", "12"), result);
    }
}
