package org.fool.framework.common;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertyTypeAdaperTest {
    @Test
    public void mapsJavaTypesToLegacyPropertyTypes() {
        assertEquals(PropertyType.Int, PropertyTypeAdaper.getPropertyType(int.class));
        assertEquals(PropertyType.Long, PropertyTypeAdaper.getPropertyType(Long.class));
        assertEquals(PropertyType.Boolean, PropertyTypeAdaper.getPropertyType(boolean.class));
        assertEquals(PropertyType.Decimal, PropertyTypeAdaper.getPropertyType(BigDecimal.class));
        assertEquals(PropertyType.String, PropertyTypeAdaper.getPropertyType(String.class));
        assertEquals(PropertyType.Date, PropertyTypeAdaper.getPropertyType(LocalDate.class));
        assertEquals(PropertyType.DateTime, PropertyTypeAdaper.getPropertyType(LocalDateTime.class));
        assertEquals(PropertyType.Guid, PropertyTypeAdaper.getPropertyType(UUID.class));
        assertEquals(PropertyType.Enum, PropertyTypeAdaper.getPropertyType(SampleState.class));
        assertEquals(PropertyType.BusinessObject, PropertyTypeAdaper.getPropertyType(Object.class));
    }

    @Test
    public void returnsLegacyDefaultsForScalarPropertyTypes() {
        assertEquals(false, PropertyTypeAdaper.getDefaultValue(PropertyType.Boolean));
        assertEquals((byte) 0, PropertyTypeAdaper.getDefaultValue(PropertyType.Byte));
        assertEquals('\0', PropertyTypeAdaper.getDefaultValue(PropertyType.Char));
        assertEquals(LocalDateTime.of(1, 1, 1, 0, 0), PropertyTypeAdaper.getDefaultValue(PropertyType.DateTime));
        assertEquals(BigDecimal.ZERO, PropertyTypeAdaper.getDefaultValue(PropertyType.Decimal));
        assertEquals(0D, PropertyTypeAdaper.getDefaultValue(PropertyType.Double));
        assertEquals(0, PropertyTypeAdaper.getDefaultValue(PropertyType.Enum));
        assertEquals(0F, PropertyTypeAdaper.getDefaultValue(PropertyType.Float));
        assertEquals(0, PropertyTypeAdaper.getDefaultValue(PropertyType.Int));
        assertEquals(0L, PropertyTypeAdaper.getDefaultValue(PropertyType.Long));
        assertEquals("", PropertyTypeAdaper.getDefaultValue(PropertyType.String));
        assertEquals(new UUID(0L, 0L), PropertyTypeAdaper.getDefaultValue(PropertyType.Guid));
        assertNull(PropertyTypeAdaper.getDefaultValue(PropertyType.BusinessObject));
    }

    @Test
    public void propertyTypesKeepLegacyCodes() {
        assertEquals(0, PropertyType.IdentifyId.code());
        assertEquals(1, PropertyType.Int.code());
        assertEquals(2, PropertyType.UInt.code());
        assertEquals(3, PropertyType.Long.code());
        assertEquals(4, PropertyType.ULong.code());
        assertEquals(5, PropertyType.Float.code());
        assertEquals(6, PropertyType.Double.code());
        assertEquals(7, PropertyType.Decimal.code());
        assertEquals(8, PropertyType.Boolean.code());
        assertEquals(9, PropertyType.Char.code());
        assertEquals(10, PropertyType.Byte.code());
        assertEquals(11, PropertyType.String.code());
        assertEquals(12, PropertyType.Date.code());
        assertEquals(13, PropertyType.Time.code());
        assertEquals(14, PropertyType.DateTime.code());
        assertEquals(15, PropertyType.Enum.code());
        assertEquals(16, PropertyType.BusinessObject.code());
        assertEquals(17, PropertyType.SerialNo.code());
        assertEquals(18, PropertyType.MD5.code());
        assertEquals(19, PropertyType.Radom.code());
        assertEquals(20, PropertyType.RadomDECS.code());
        assertEquals(21, PropertyType.Guid.code());
    }

    private enum SampleState {
        READY
    }
}
