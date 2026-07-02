package org.fool.framework.common.annotation;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class EnumNoteTest {
    @Test
    public void exposesLegacyEnumNoteAtRuntime() throws Exception {
        Field field = State.class.getDeclaredField("OPEN");
        EnumNote note = field.getAnnotation(EnumNote.class);

        assertEquals("Open order", note.value());
    }

    private enum State {
        @EnumNote("Open order")
        OPEN
    }
}
