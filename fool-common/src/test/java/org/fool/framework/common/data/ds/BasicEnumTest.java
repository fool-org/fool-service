package org.fool.framework.common.data.ds;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BasicEnumTest {
    @Test
    public void registersValuesByChildType() {
        assertEquals(1, TestStatus.ACTIVE.getValue());
        assertEquals(1, TestStatus.ACTIVE.intValue());
        assertEquals("Active", TestStatus.ACTIVE.getState());
        assertEquals("Active", TestStatus.ACTIVE.toString());
        assertEquals(TestStatus.ACTIVE, TestStatus.get(1));
        assertNull(TestStatus.get(3));
        assertArrayEquals(new BasicEnum[]{TestStatus.ACTIVE, TestStatus.DISABLED}, BasicEnum.all(TestStatus.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsDuplicateValuesWithinSameChildType() {
        new TestStatus(1, "Duplicate");
    }

    private static final class TestStatus extends BasicEnum {
        private static final TestStatus ACTIVE = new TestStatus(1, "Active");
        private static final TestStatus DISABLED = new TestStatus(2, "Disabled");

        private TestStatus(int value, String state) {
            super(value, state, TestStatus.class);
        }

        private static TestStatus get(int value) {
            return getBy(value, TestStatus.class);
        }
    }
}
