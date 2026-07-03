package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.SaveObjRequest;
import org.fool.framework.view.model.View;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataQueryServiceSaveObjTest {
    @Test
    public void saveLegacyObjectWritesSimplePropertiesToDynamicData() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View view = new View();
        view.setViewModel("Order");
        Model order = model();
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(order);
        when(modelDataService.saveData(any(IDynamicData.class))).thenReturn(true);
        SaveObjRequest request = new SaveObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1001");
        saveObj.setViewID("100");
        saveObj.setPropertyies(List.of(
                new SaveObjRequest.SaveKeypair("symbol", "ETH-USDT"),
                new SaveObjRequest.SaveKeypair("state", "OPEN")));
        request.setSaveObj(saveObj);

        service.saveLegacyObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).saveData(dataCaptor.capture());
        IDynamicData data = dataCaptor.getValue();
        assertEquals("1001", data.getId());
        assertEquals("1001", data.get("orderId"));
        assertEquals("ETH-USDT", data.get("symbol"));
        assertEquals("OPEN", data.get("state"));
    }

    @Test
    public void saveLegacyObjectWritesItemPropertiesToDynamicSubItems() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View view = new View();
        view.setViewModel("Order");
        Model order = modelWithItems();
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(order);
        when(modelDataService.saveData(any(IDynamicData.class))).thenReturn(true);
        SaveObjRequest request = new SaveObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1001");
        saveObj.setViewID("100");
        SaveObjRequest.ItemProperty items = new SaveObjRequest.ItemProperty();
        items.setKey("items");
        items.setItems(List.of(item("I2", true, new SaveObjRequest.SaveKeypair("itemName", "After child"))));
        items.setAddedItems(List.of(item("I3", true, new SaveObjRequest.SaveKeypair("itemName", "New child"))));
        items.setDelteItems(List.of(item("I4", true)));
        saveObj.setItemproperties(List.of(items));
        request.setSaveObj(saveObj);

        service.saveLegacyObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).saveData(dataCaptor.capture());
        Object value = dataCaptor.getValue().get("items");
        assertTrue(value instanceof SubItemList<?>);
        SubItemList<?> subItems = (SubItemList<?>) value;
        assertEquals(2, subItems.size());
        assertItem("I2", "After child", subItems.get(0));
        assertItem("I3", "New child", subItems.get(1));
        assertEquals(1, subItems.getDeleteList().size());
        assertItem("I4", null, subItems.getDeleteList().get(0));
    }

    @Test
    public void saveLegacyObjectUsesModelServiceMetadataForItemProperties() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View view = new View();
        view.setViewModel("Order");
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model());
        when(modelDataService.getModel("Order")).thenReturn(modelWithItems());
        when(modelDataService.saveData(any(IDynamicData.class))).thenReturn(true);
        SaveObjRequest request = new SaveObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1001");
        saveObj.setViewID("100");
        SaveObjRequest.ItemProperty items = new SaveObjRequest.ItemProperty();
        items.setKey("items");
        items.setItems(List.of(item("I2", true, new SaveObjRequest.SaveKeypair("itemName", "After child"))));
        saveObj.setItemproperties(List.of(items));
        request.setSaveObj(saveObj);

        service.saveLegacyObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).saveData(dataCaptor.capture());
        assertTrue(dataCaptor.getValue().get("items") instanceof SubItemList<?>);
    }

    private static Model model() {
        Model model = new Model();
        model.setName("Order");
        model.setTableName("market_order");
        Property orderId = property("orderId", "order_id", PropertyType.Long);
        model.setIdProperty(orderId);
        model.setProperties(List.of(
                orderId,
                property("symbol", "order_symbol", PropertyType.String),
                property("state", "order_state", PropertyType.String)));
        return model;
    }

    private static Model modelWithItems() {
        Model item = new Model();
        item.setName("OrderItem");
        item.setTableName("market_order_item");
        Property itemId = property("itemId", "item_id", PropertyType.String);
        item.setIdProperty(itemId);
        item.setProperties(List.of(
                itemId,
                property("itemName", "item_name", PropertyType.String)));

        Model order = model();
        Property items = property("items", null, PropertyType.BusinessObject);
        items.setIsCollection(true);
        items.setPropertyModel(item);
        order.setProperties(List.of(
                order.getProperties().get(0),
                order.getProperties().get(1),
                order.getProperties().get(2),
                items));
        return order;
    }

    private static Property property(String name, String column, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        property.setPropertyType(type);
        property.setIsCollection(false);
        return property;
    }

    private static SaveObjRequest.Item item(String id, boolean isExist, SaveObjRequest.SaveKeypair... propertyies) {
        SaveObjRequest.Item item = new SaveObjRequest.Item();
        item.setItemId(id);
        item.setExist(isExist);
        item.setPropertyies(List.of(propertyies));
        return item;
    }

    private static void assertItem(String id, String itemName, Object item) {
        assertTrue(item instanceof IDynamicData);
        IDynamicData data = (IDynamicData) item;
        assertEquals(id, data.get("itemId"));
        assertEquals(itemName, data.get("itemName"));
    }
}
