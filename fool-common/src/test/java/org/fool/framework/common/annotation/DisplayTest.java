package org.fool.framework.common.annotation;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DisplayTest {
    @Test
    public void exposesLegacyShowDescriptionMetadataAtRuntime() throws Exception {
        Field shown = Sample.class.getDeclaredField("shown");
        Display display = shown.getAnnotation(Display.class);

        assertEquals("Name", display.displayName());
        assertTrue(display.display());
        assertEquals(3, display.displayIndex());
        assertTrue(display.editable());
        assertTrue(display.generationDropdownList());
        assertFalse(display.showInList());
    }

    @Test
    public void showInListDefaultsToLegacyTrue() throws Exception {
        Display display = Sample.class.getDeclaredField("defaulted").getAnnotation(Display.class);

        assertFalse(display.display());
        assertTrue(display.showInList());
    }

    private static class Sample {
        @Display(
                displayName = "Name",
                display = true,
                displayIndex = 3,
                editable = true,
                generationDropdownList = true,
                showInList = false)
        private String shown;

        @Display
        private String defaulted;
    }
}
