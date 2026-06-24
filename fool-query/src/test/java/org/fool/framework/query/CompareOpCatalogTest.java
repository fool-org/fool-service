package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompareOpCatalogTest {

    @Test
    public void legacyCatalogMapsRowsForPropertyType() {
        JdbcCompareOpCatalog catalog = new JdbcCompareOpCatalog(propertyType -> List.of(
                Map.of(
                        "SysID", 1L,
                        "SE_COMPARESHOW", "等于",
                        "SE_COMPAREEXP", "{0} = {1}"),
                Map.of(
                        "SysID", 2L,
                        "SE_COMPARESHOW", "包含",
                        "SE_COMPAREEXP", "{0} LIKE {1}")));

        List<LegacyCompareOp> operations = catalog.listFor(PropertyType.String);

        assertEquals(2, operations.size());
        assertEquals(1L, operations.get(0).getId());
        assertEquals("等于", operations.get(0).getShowName());
        assertEquals("{0} = {1}", operations.get(0).getDbName());
        assertEquals(PropertyType.String, operations.get(0).getPropertyType());
        assertEquals("包含", operations.get(1).getShowName());
    }

    @Test
    public void legacyCatalogOverloadIgnoresPropertyIdLikeCompareOpFac() {
        AtomicReference<PropertyType> requestedType = new AtomicReference<>();
        JdbcCompareOpCatalog catalog = new JdbcCompareOpCatalog(propertyType -> {
            requestedType.set(propertyType);
            return List.of(Map.of(
                    "SysID", 9L,
                    "SE_COMPARESHOW", "大于",
                    "SE_COMPAREEXP", "{0} > {1}"));
        });

        List<LegacyCompareOp> operations = catalog.listFor(PropertyType.Decimal, 1001L);

        assertEquals(PropertyType.Decimal, requestedType.get());
        assertEquals(1, operations.size());
        assertEquals(9L, operations.get(0).getId());
    }

    @Test
    public void jdbcCatalogUsesLegacyCompareTypeTablesAndPropertyTypeOrdinal() {
        assertTrue(JdbcCompareOpCatalog.SELECT_SQL.contains("SE_COMPARETYPE"));
        assertTrue(JdbcCompareOpCatalog.SELECT_SQL.contains("SE_COMPARETYPE_PROPERTYINDEX"));
        assertEquals(11, PropertyType.String.ordinal());
    }
}
