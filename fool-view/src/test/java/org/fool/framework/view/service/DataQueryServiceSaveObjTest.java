package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.LegacySaveNewObjRequest;
import org.fool.framework.view.dto.SaveObjRequest;
import org.fool.framework.view.model.View;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    public void saveLegacyObjectPreservesUnpostedExistingProperties() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View view = new View();
        view.setViewModel("Order");
        Model order = modelWithItems();
        DbMysqlDynamic existing = new DbMysqlDynamic(order);
        existing.set("orderId", "1001");
        existing.set("symbol", "BTC-USDT");
        existing.set("state", "OPEN");
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(order);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(existing);
        when(modelDataService.saveData(any(IDynamicData.class))).thenReturn(true);
        SaveObjRequest request = new SaveObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1001");
        saveObj.setViewID("100");
        saveObj.setPropertyies(List.of(new SaveObjRequest.SaveKeypair("state", "FILLED")));
        SaveObjRequest.ItemProperty items = new SaveObjRequest.ItemProperty();
        items.setKey("items");
        items.setAddedItems(List.of(item("I3", true, new SaveObjRequest.SaveKeypair("itemName", "New child"))));
        saveObj.setItemproperties(List.of(items));
        request.setSaveObj(saveObj);

        service.saveLegacyObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).saveData(dataCaptor.capture());
        IDynamicData data = dataCaptor.getValue();
        assertEquals("BTC-USDT", data.get("symbol"));
        assertEquals("FILLED", data.get("state"));
        assertTrue(data.get("items") instanceof SubItemList<?>);
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

    @Test
    public void saveLegacyNewObjectCreatesSimpleObject() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View view = new View();
        view.setViewModel("Order");
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model());
        LegacySaveNewObjRequest request = new LegacySaveNewObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1003");
        saveObj.setViewID("100");
        saveObj.setPropertyies(List.of(new SaveObjRequest.SaveKeypair("symbol", "SOL-USDT")));
        request.setSaveObj(saveObj);

        service.saveLegacyNewObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).createData(dataCaptor.capture());
        assertEquals("1003", dataCaptor.getValue().getId());
        assertEquals("SOL-USDT", dataCaptor.getValue().get("symbol"));
    }

    @Test
    public void saveLegacyNewObjectCreatesOwnedCollectionItem() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View childView = new View();
        childView.setViewModel("OrderItem");
        View ownerView = new View();
        ownerView.setViewModel("Order");
        when(daoService.getOneDetailByKey(View.class, "200")).thenReturn(childView);
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(ownerView);
        when(modelDataService.getModel("OrderItem")).thenReturn(itemModel());
        when(modelDataService.getModel("Order")).thenReturn(modelWithItems());
        LegacySaveNewObjRequest request = new LegacySaveNewObjRequest();
        request.setOwnerViewId("100");
        request.setOwnerId("1001");
        request.setProperty("items");
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("2009");
        saveObj.setViewID("200");
        saveObj.setPropertyies(List.of(new SaveObjRequest.SaveKeypair("itemName", "New child")));
        request.setSaveObj(saveObj);

        service.saveLegacyNewObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).createData(dataCaptor.capture(), eq("order_id"), eq("1001"));
        assertEquals("2009", dataCaptor.getValue().getId());
        assertEquals("New child", dataCaptor.getValue().get("itemName"));
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
        Model item = itemModel();
        Model order = model();
        Property items = property("items", null, PropertyType.BusinessObject);
        items.setIsCollection(true);
        items.setPropertyModel(item);
        Relation relation = new Relation();
        relation.setRelationType(RelationType.One2Many);
        relation.setProperty(items);
        relation.setTargetColumn("order_id");
        order.setProperties(List.of(
                order.getProperties().get(0),
                order.getProperties().get(1),
                order.getProperties().get(2),
                items));
        order.setRelations(List.of(relation));
        return order;
    }

    private static Model itemModel() {
        Model item = new Model();
        item.setName("OrderItem");
        item.setTableName("market_order_item");
        Property itemId = property("itemId", "item_id", PropertyType.String);
        item.setIdProperty(itemId);
        item.setProperties(List.of(
                itemId,
                property("itemName", "item_name", PropertyType.String)));
        return item;
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
