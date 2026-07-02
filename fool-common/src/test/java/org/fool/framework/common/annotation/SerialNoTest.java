package org.fool.framework.common.annotation;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class SerialNoTest {
    @Test
    public void exposesLegacySerialNoMetadataAtRuntime() throws Exception {
        Field no = Sample.class.getDeclaredField("no");
        SerialNo serialNo = no.getAnnotation(SerialNo.class);

        assertEquals(3, serialNo.len());
        assertEquals("yyMMdd", serialNo.dateFormat());
        assertEquals("TE", serialNo.serialPrefix());
    }

    private static class Sample {
        @SerialNo(len = 3, dateFormat = "yyMMdd", serialPrefix = "TE")
        private String no;
    }
}
