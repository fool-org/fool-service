package org.fool.framework.query;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SelectTypeCatalogTest {

    @Test
    public void legacyCatalogMapsRowsForPropertyType() {
        JdbcSelectTypeCatalog catalog = new JdbcSelectTypeCatalog(
                propertyType -> List.of(
                        Map.of(
                                "SysID", 1L,
                                "SE_SELECTEDSHOW", "原值",
                                "SE_SELECTEDEXP", "{0}",
                                "SE_REQUIREGROUP", true),
                        Map.of(
                                "SysID", 3L,
                                "SE_SELECTEDSHOW", "求和",
                                "SE_SELECTEDEXP", "SUM({0})",
                                "SE_REQUIREGROUP", false)),
                List::of);

        List<SelectType> selectTypes = catalog.listFor(PropertyType.Decimal);

        assertEquals(2, selectTypes.size());
        assertEquals(1L, selectTypes.get(0).getId());
        assertEquals("原值", selectTypes.get(0).getShow());
        assertEquals("{0}", selectTypes.get(0).getDbExp());
        assertTrue(selectTypes.get(0).isRequireGroupCol());
        assertEquals("求和", selectTypes.get(1).getShow());
        assertEquals("SUM({0})", selectTypes.get(1).getDbExp());
        assertFalse(selectTypes.get(1).isRequireGroupCol());
    }

    @Test
    public void legacyCatalogOverloadIgnoresPropertyIdLikeSelectedTypeFac() {
        AtomicReference<PropertyType> requestedType = new AtomicReference<>();
        JdbcSelectTypeCatalog catalog = new JdbcSelectTypeCatalog(
                propertyType -> {
                    requestedType.set(propertyType);
                    return List.of(Map.of(
                            "SysID", 2L,
                            "SE_SELECTEDSHOW", "计数",
                            "SE_SELECTEDEXP", "COUNT({0})",
                            "SE_REQUIREGROUP", 0));
                },
                List::of);

        List<SelectType> selectTypes = catalog.listFor(PropertyType.String, 1001L);

        assertEquals(PropertyType.String, requestedType.get());
        assertEquals(1, selectTypes.size());
        assertEquals(2L, selectTypes.get(0).getId());
        assertFalse(selectTypes.get(0).isRequireGroupCol());
    }

    @Test
    public void legacyCatalogListsAllSelectedTypes() {
        JdbcSelectTypeCatalog catalog = new JdbcSelectTypeCatalog(
                propertyType -> List.of(),
                () -> List.of(Map.of(
                        "SysID", 6L,
                        "SE_SELECTEDSHOW", "最小",
                        "SE_SELECTEDEXP", "MIN({0})",
                        "SE_REQUIREGROUP", false)));

        List<SelectType> selectTypes = catalog.listAll();

        assertEquals(1, selectTypes.size());
        assertEquals(6L, selectTypes.get(0).getId());
        assertEquals("最小", selectTypes.get(0).getShow());
        assertEquals("MIN({0})", selectTypes.get(0).getDbExp());
    }

    @Test
    public void jdbcCatalogUsesLegacySelectedTypeTablesAndPropertyTypeOrdinal() {
        assertTrue(JdbcSelectTypeCatalog.SELECT_SQL.contains("SE_SELECTEDTYPE"));
        assertTrue(JdbcSelectTypeCatalog.SELECT_SQL.contains("SE_SELECTEDTYPE_PROPERTYINDEX"));
        assertEquals(11, PropertyType.String.ordinal());
    }
}
