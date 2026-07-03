package org.fool.framework.model.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.junit.Test;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapperDbMapsTest {
    @Test
    public void mapsLegacyMultiDbMapBusinessObjectFromCurrentRowColumns() throws Exception {
        Model customer = model("Customer", "SW_CUSTOMER");
        Property customerId = property("customerId", "CUSTOMER_ID", PropertyType.Long, false);
        Property displayName = property("displayName", "DISPLAY_NAME", PropertyType.String, false);
        customer.setIdProperty(customerId);
        customer.setProperties(List.of(customerId, displayName));

        Model order = model("Order", "SW_ORDER");
        Property orderId = property("orderId", "ORDER_ID", PropertyType.Long, false);
        order.setIdProperty(orderId);
        Property customerSnapshot = property("customer", null, PropertyType.BusinessObject, false);
        customerSnapshot.setPropertyModel(customer);
        customerSnapshot.setMultiMap(true);
        customerSnapshot.setDbMaps(List.of(
                new MultiDbMap("customerId", "CUSTOMER_ID"),
                new MultiDbMap("displayName", "CUSTOMER_NAME")));
        order.setProperties(List.of(orderId, customerSnapshot));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("ORDER_ID")).thenReturn(1001L);
        when(resultSet.getObject("CUSTOMER_ID")).thenReturn(42L);
        when(resultSet.getObject("CUSTOMER_NAME")).thenReturn("Ada");

        IDynamicData mapped = new Mapper(order).mapRow(resultSet, 0);

        assertEquals("1001", mapped.getId());
        Object customerValue = mapped.get("customer");
        assertNotNull(customerValue);
        assertTrue(customerValue instanceof IDynamicData);
        IDynamicData customerData = (IDynamicData) customerValue;
        assertEquals(42L, customerData.get("customerId"));
        assertEquals("Ada", customerData.get("displayName"));
    }

    @Test
    public void mapsLegacyMultiDbMapBusinessObjectFromListQueryAliases() throws Exception {
        Model customer = model("Customer", "SW_CUSTOMER");
        Property customerId = property("customerId", "CUSTOMER_ID", PropertyType.Long, false);
        Property displayName = property("displayName", "DISPLAY_NAME", PropertyType.String, false);
        customer.setIdProperty(customerId);
        customer.setProperties(List.of(customerId, displayName));

        Model order = model("Order", "SW_ORDER");
        Property customerSnapshot = property("customer", null, PropertyType.BusinessObject, false);
        customerSnapshot.setPropertyModel(customer);
        customerSnapshot.setMultiMap(true);
        customerSnapshot.setDbMaps(List.of(
                new MultiDbMap("customerId", "CUSTOMER_ID"),
                new MultiDbMap("displayName", "CUSTOMER_NAME")));
        order.setProperties(List.of(customerSnapshot));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("customer_customerId")).thenReturn(42L);
        when(resultSet.getObject("customer_displayName")).thenReturn("Ada");

        IDynamicData mapped = new Mapper(order).mapRow(resultSet, 0);

        Object customerValue = mapped.get("customer");
        assertNotNull(customerValue);
        assertTrue(customerValue instanceof IDynamicData);
        IDynamicData customerData = (IDynamicData) customerValue;
        assertEquals(42L, customerData.get("customerId"));
        assertEquals("Ada", customerData.get("displayName"));
    }

    @Test
    public void mapsLegacyJoinedBusinessObjectFromListQueryAliases() throws Exception {
        Model customer = model("Customer", "SW_CUSTOMER");
        Property customerId = property("customerId", "CUSTOMER_ID", PropertyType.Long, false);
        Property displayName = property("displayName", "DISPLAY_NAME", PropertyType.String, false);
        customer.setIdProperty(customerId);
        customer.setShowProperty(displayName);
        customer.setProperties(List.of(customerId, displayName));

        Model order = model("Order", "SW_ORDER");
        Property orderId = property("orderId", "ORDER_ID", PropertyType.Long, false);
        Property customerProperty = property("customer", "CUSTOMER_ID", PropertyType.BusinessObject, false);
        customerProperty.setPropertyModel(customer);
        order.setProperties(List.of(orderId, customerProperty));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("ORDER_ID")).thenReturn(1001L);
        when(resultSet.getObject("customer_CUSTOMER_ID")).thenReturn(42L);
        when(resultSet.getObject("customer_DISPLAY_NAME")).thenReturn("Ada");

        IDynamicData mapped = new Mapper(order).mapRow(resultSet, 0);

        Object customerValue = mapped.get("customer");
        assertNotNull(customerValue);
        assertTrue(customerValue instanceof IDynamicData);
        IDynamicData customerData = (IDynamicData) customerValue;
        assertEquals(42L, customerData.get("customerId"));
        assertEquals("Ada", customerData.get("displayName"));
    }

    @Test
    public void mapsLegacyJoinedBusinessObjectWithMissingShowProperty() throws Exception {
        Model customer = model("Customer", "SW_CUSTOMER");
        Property customerId = property("customerId", "CUSTOMER_ID", PropertyType.Long, false);
        Property displayName = property("displayName", "DISPLAY_NAME", PropertyType.String, false);
        customer.setIdProperty(customerId);
        customer.setProperties(List.of(customerId, displayName));

        Model order = model("Order", "SW_ORDER");
        Property customerProperty = property("customer", "ORDER_CUSTOMER_ID", PropertyType.BusinessObject, false);
        customerProperty.setPropertyModel(customer);
        order.setProperties(List.of(customerProperty));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("customer_CUSTOMER_ID")).thenReturn(42L);
        when(resultSet.getObject("customer_DISPLAY_NAME")).thenReturn("Ada");

        IDynamicData mapped = new Mapper(order).mapRow(resultSet, 0);

        Object customerValue = mapped.get("customer");
        assertNotNull(customerValue);
        assertTrue(customerValue instanceof IDynamicData);
        IDynamicData customerData = (IDynamicData) customerValue;
        assertEquals(42L, customerData.get("customerId"));
        assertEquals("Ada", customerData.get("displayName"));
    }

    @Test
    public void mapsLegacyDefaultValuesForNullSimpleColumns() throws Exception {
        Model order = model("Order", "SW_ORDER");
        Property active = property("active", "ACTIVE", PropertyType.Boolean, false);
        Property count = property("count", "COUNT", PropertyType.Int, false);
        Property total = property("total", "TOTAL", PropertyType.Long, false);
        Property amount = property("amount", "AMOUNT", PropertyType.Decimal, false);
        Property code = property("code", "CODE", PropertyType.String, false);
        Property serial = property("serial", "SERIAL", PropertyType.SerialNo, false);
        Property createdAt = property("createdAt", "CREATED_AT", PropertyType.DateTime, false);
        order.setProperties(List.of(active, count, total, amount, code, serial, createdAt));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("ACTIVE")).thenReturn(null);
        when(resultSet.getObject("COUNT")).thenReturn(null);
        when(resultSet.getObject("TOTAL")).thenReturn(null);
        when(resultSet.getObject("AMOUNT")).thenReturn(null);
        when(resultSet.getObject("CODE")).thenReturn(null);
        when(resultSet.getObject("SERIAL")).thenReturn(null);
        when(resultSet.getObject("CREATED_AT")).thenReturn(null);

        IDynamicData mapped = new Mapper(order).mapRow(resultSet, 0);

        assertEquals(false, mapped.get("active"));
        assertEquals(0, mapped.get("count"));
        assertEquals(0L, mapped.get("total"));
        assertEquals(BigDecimal.ZERO, mapped.get("amount"));
        assertEquals("", mapped.get("code"));
        assertEquals("", mapped.get("serial"));
        assertNull(mapped.get("createdAt"));
    }

    @Test
    public void mapsLegacyEnumDefaultToFirstEnumValueForNullColumn() throws Exception {
        Model state = model("OrderState", null);
        state.setModelType(ModelType.ENUM);
        setEnumValues(state, List.of(enumValue("OPEN", "0"), enumValue("CLOSED", "1")));

        Model order = model("Order", "SW_ORDER");
        Property status = property("status", "STATUS", PropertyType.Enum, false);
        status.setPropertyModel(state);
        order.setProperties(List.of(status));

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("STATUS")).thenReturn(null);

        IDynamicData mapped = new Mapper(order).mapRow(resultSet, 0);

        assertEquals(0, mapped.get("status"));
    }

    private static Model model(String name, String tableName) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName(tableName);
        model.setAutoSysId(false);
        return model;
    }

    private static Property property(String name, String column, PropertyType type, boolean collection) {
        Property property = new Property();
        property.setName(name);
        property.setRemark(name);
        property.setColumn(column);
        property.setPropertyType(type);
        property.setIsCollection(collection);
        property.setAllowDbNull(false);
        property.setCheck(false);
        return property;
    }

    private static EnumValue enumValue(String name, String value) {
        EnumValue enumValue = new EnumValue();
        enumValue.setName(name);
        enumValue.setValue(value);
        return enumValue;
    }

    private static void setEnumValues(Model model, List<EnumValue> values) {
        try {
            Field field = Model.class.getDeclaredField("enumValues");
            field.setAccessible(true);
            field.set(model, values);
        } catch (ReflectiveOperationException ex) {
        }
    }
}
