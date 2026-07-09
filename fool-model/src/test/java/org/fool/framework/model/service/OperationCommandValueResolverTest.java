package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    @Test
    public void resolvesLegacyOwnerPropertyExpressions() {
        Model model = new Model();
        DbMysqlDynamic owner = new DbMysqlDynamic(model);
        owner.set("orderName", "parent order");
        DbMysqlDynamic child = new DbMysqlDynamic(model);
        child.setOwner(owner);

        Object value = new OperationCommandValueResolver().resolve(
                null,
                child,
                "#.orderName",
                (property, raw) -> raw);

        assertEquals("parent order", value);
    }

    @Test
    public void resolvesLegacyLongStaticValuesAsLong() {
        Property property = new Property();
        property.setPropertyType(PropertyType.Long);

        Object value = new OperationCommandValueResolver().resolve(
                property,
                null,
                "$1000",
                (targetProperty, raw) -> raw);

        assertEquals(Long.valueOf(1000L), value);
    }

    @Test
    public void resolvesLegacyIdentifyIdStaticValuesAsLong() {
        Property property = new Property();
        property.setPropertyType(PropertyType.IdentifyId);

        Object value = new OperationCommandValueResolver().resolve(
                property,
                null,
                "$1000",
                (targetProperty, raw) -> raw);

        assertEquals(Long.valueOf(1000L), value);
    }

    @Test
    public void resolvesLegacyDateTimeStaticDateOnlyValues() {
        Property property = new Property();
        property.setPropertyType(PropertyType.DateTime);

        try {
            Object value = new OperationCommandValueResolver().resolve(
                    property,
                    null,
                    "$2026-07-03",
                    (targetProperty, raw) -> raw);

            assertEquals(LocalDateTime.of(2026, 7, 3, 0, 0), value);
        } catch (RuntimeException ex) {
            fail("date-only DateTime static values should parse: " + ex.getMessage());
        }
    }
}
