package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.dto.CommonException;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.InputQueryRequest;
import org.fool.framework.view.dto.InputQueryResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataQueryServiceInputQueryTest {
    @Test
    public void inputQueryReturnsLegacyIdTextItemsFromViewItemModel() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = model("Customer", "customer", "customerId", "customer_id", "customerName", "customer_name");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = new Model();
        order.setName("Order");
        order.setProperties(List.of(customerProperty));
        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(viewItem("Customer", "customer")));
        PageResult<IDynamicData> pageResult = new PageResult<>();
        pageResult.setItems(List.of(dynamic("1001", "Ada"), dynamic("1002", "Adam")));
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true)))
                .thenReturn(pageResult);
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(100L);
        request.setViewItemId("Customer");
        request.setText("Ad");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(2, result.getItems().size());
        assertEquals("1001", result.getItems().get(0).getId());
        assertEquals("Ada", result.getItems().get(0).getText());
        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        ArgumentCaptor<PageNavigator> pageCaptor = ArgumentCaptor.forClass(PageNavigator.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Customer"),
                filterCaptor.capture(),
                anyList(),
                pageCaptor.capture(),
                eq("customer_id"),
                eq(true));
        assertEquals("`customer_name` LIKE ?", filterCaptor.getValue().generateSql().getSql());
        assertArrayEquals(new Object[]{"%Ad%"}, filterCaptor.getValue().generateSql().getArgs());
        assertEquals(1, pageCaptor.getValue().getPageIndex());
        assertEquals(5, pageCaptor.getValue().getPageSize());
    }

    @Test
    public void inputQueryResolvesLegacyViewIdBeforeViewName() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Property customerProperty = property("customer", "customer_id");
        Model order = new Model();
        order.setName("Order");
        order.setProperties(List.of(customerProperty));
        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(viewItem("Customer", "customer")));
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(100L);
        request.setViewName("WrongViewName");
        request.setViewItemId("Customer");

        service.inputQuery(request);

        verify(daoService).getOneDetailByKey(View.class, "100");
        verify(daoService, never()).getOneDetailByKey(View.class, "WrongViewName");
    }

    @Test
    public void inputQueryRejectsViewNameOnlyRequest() {
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", mock(DaoService.class));
        InputQueryRequest request = new InputQueryRequest();
        request.setViewName("BusinessNameShortcut");
        request.setViewItemId("Customer");

        CommonException exception = assertThrows(CommonException.class, () -> service.inputQuery(request));

        assertEquals("ViewId is required", exception.getMessage());
    }

    @Test
    public void inputQueryUsesFirstStringPropertyWhenShowPropertyIsMissing() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = new Model();
        customer.setName("Customer");
        Property customerId = property("customerId", "customer_id");
        customerId.setPropertyType(PropertyType.Long);
        Property displayName = property("displayName", "display_name");
        displayName.setPropertyType(PropertyType.String);
        customer.setIdProperty(customerId);
        customer.setProperties(List.of(customerId, displayName));
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = new Model();
        order.setName("Order");
        order.setProperties(List.of(customerProperty));
        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(viewItem("Customer", "customer")));
        IDynamicData ada = mock(IDynamicData.class);
        when(ada.getId()).thenReturn("2001");
        when(ada.get("displayName")).thenReturn("Ada Capital");
        PageResult<IDynamicData> pageResult = new PageResult<>();
        pageResult.setItems(List.of(ada));
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true)))
                .thenReturn(pageResult);
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(100L);
        request.setViewItemId("Customer");
        request.setText("Ada");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(1, result.getItems().size());
        assertEquals("2001", result.getItems().get(0).getId());
        assertEquals("Ada Capital", result.getItems().get(0).getText());
        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Customer"),
                filterCaptor.capture(),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true));
        assertEquals("`display_name` LIKE ?", filterCaptor.getValue().generateSql().getSql());
        assertArrayEquals(new Object[]{"%Ada%"}, filterCaptor.getValue().generateSql().getArgs());
    }

    @Test
    public void inputQueryCombinesSelectedViewFilterWithTextLookup() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = model("Customer", "customer", "customerId", "customer_id", "customerName", "customer_name");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = new Model();
        order.setName("Order");
        order.setProperties(List.of(customerProperty));
        ViewItem customerItem = viewItem("Customer", "customer");
        customerItem.setSelectedViewId(201L);
        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(customerItem));
        View selectedView = new View();
        selectedView.setId(201L);
        selectedView.setViewModel("Customer");
        selectedView.setFilter("customer_state='active'");
        PageResult<IDynamicData> pageResult = new PageResult<>();
        pageResult.setItems(List.of(dynamic("1001", "Ada")));
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(View.class, "201")).thenReturn(selectedView);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true)))
                .thenReturn(pageResult);
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(100L);
        request.setViewItemId("Customer");
        request.setText("Ada");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(1, result.getItems().size());
        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Customer"),
                filterCaptor.capture(),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true));
        assertEquals("(customer_state='active') And (`customer_name` LIKE ?)", filterCaptor.getValue().generateSql().getSql());
        assertArrayEquals(new Object[]{"%Ada%"}, filterCaptor.getValue().generateSql().getArgs());
    }

    @Test
    public void inputQueryFiltersLegacySourceListForExistingObject() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = model("Customer", "customer", "customerId", "customer_id", "customerName", "customer_name");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        customerProperty.setSource("availableCustomers");
        Model order = model("Order", "market_order", "orderId", "order_id", "orderId", "order_id");
        order.setProperties(List.of(order.getIdProperty(), customerProperty));
        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(viewItem("Customer", "customer")));
        IDynamicData owner = mock(IDynamicData.class);
        IDynamicData alice = dynamic("1001", "Alice");
        IDynamicData bob = dynamic("1002", "Bob");
        IDynamicData alina = dynamic("1003", "ALINA");
        when(owner.get("availableCustomers")).thenReturn(List.of(alice, bob, alina));
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataList(eq("Order"), any(IQueryFilter.class), eq(order.getProperties())))
                .thenReturn(List.of(owner));
        PageResult<IDynamicData> emptyPage = new PageResult<>();
        emptyPage.setItems(List.of());
        when(modelDataService.getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true)))
                .thenReturn(emptyPage);
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(100L);
        request.setViewItemId("Customer");
        request.setObjID("5001");
        request.setText("ali");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(2, result.getItems().size());
        assertEquals("1001", result.getItems().get(0).getId());
        assertEquals("Alice", result.getItems().get(0).getText());
        assertEquals("1003", result.getItems().get(1).getId());
        assertEquals("ALINA", result.getItems().get(1).getText());
        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataList(eq("Order"), filterCaptor.capture(), eq(order.getProperties()));
        assertEquals("`order_id`= ?", filterCaptor.getValue().generateSql().getSql());
        assertArrayEquals(new Object[]{"5001"}, filterCaptor.getValue().generateSql().getArgs());
        verify(modelDataService, never()).getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true));
    }

    @Test
    public void inputQueryUsesViewItemSourceExpressionBeforePropertySource() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = model("Customer", "customer", "customerId", "customer_id", "customerName", "customer_name");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        customerProperty.setSource("propertyCustomers");
        Model order = model("Order", "market_order", "orderId", "order_id", "orderId", "order_id");
        order.setProperties(List.of(order.getIdProperty(), customerProperty));
        ViewItem customerItem = viewItem("Customer", "customer");
        customerItem.setSourceExpression("viewCustomers");
        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(customerItem));
        IDynamicData owner = mock(IDynamicData.class);
        IDynamicData alice = dynamic("1001", "Alice");
        IDynamicData bob = dynamic("1002", "Bob");
        IDynamicData alina = dynamic("1003", "ALINA");
        when(owner.get("viewCustomers")).thenReturn(List.of(alice, alina));
        when(owner.get("propertyCustomers")).thenReturn(List.of(bob));
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataList(eq("Order"), any(IQueryFilter.class), eq(order.getProperties())))
                .thenReturn(List.of(owner));
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(100L);
        request.setViewItemId("Customer");
        request.setObjID("5001");
        request.setText("ali");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(2, result.getItems().size());
        assertEquals("1001", result.getItems().get(0).getId());
        assertEquals("1003", result.getItems().get(1).getId());
        verify(owner).get("viewCustomers");
        verify(owner, never()).get("propertyCustomers");
    }

    @Test
    public void inputQueryFiltersAddedItemSourceListFromOwnerContext() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = model("Customer", "customer", "customerId", "customer_id", "customerName", "customer_name");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = model("Order", "market_order", "orderId", "order_id", "orderId", "order_id");
        Model orderItem = model("OrderItem", "market_order_item", "itemId", "item_id", "itemName", "item_name");
        orderItem.setOwner(order);
        orderItem.setProperties(List.of(orderItem.getIdProperty(), customerProperty));
        ViewItem customerItem = viewItem("Customer", "customer");
        customerItem.setSourceExpression("#.availableCustomers");
        View view = new View();
        view.setViewName("OrderItemEdit");
        view.setViewModel("OrderItem");
        view.setListItems(List.of(customerItem));
        IDynamicData owner = mock(IDynamicData.class);
        IDynamicData alice = dynamic("1001", "Alice");
        IDynamicData bob = dynamic("1002", "Bob");
        IDynamicData alina = dynamic("1003", "ALINA");
        when(owner.get("availableCustomers")).thenReturn(List.of(alice, bob, alina));
        when(daoService.getOneDetailByKey(View.class, "200")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "OrderItem")).thenReturn(orderItem);
        when(modelDataService.getDataList(eq("Order"), any(IQueryFilter.class), eq(order.getProperties())))
                .thenReturn(List.of(owner));
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(200L);
        request.setViewItemId("Customer");
        request.setOwnerId("5001");
        request.setAdded(true);
        request.setText("ali");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(2, result.getItems().size());
        assertEquals("1001", result.getItems().get(0).getId());
        assertEquals("1003", result.getItems().get(1).getId());
        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataList(eq("Order"), filterCaptor.capture(), eq(order.getProperties()));
        assertEquals("`order_id`= ?", filterCaptor.getValue().generateSql().getSql());
        assertArrayEquals(new Object[]{"5001"}, filterCaptor.getValue().generateSql().getArgs());
        verify(modelDataService, never()).getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true));
    }

    @Test
    public void inputQueryFiltersExistingItemSourceListFromOwnerContext() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        Model customer = model("Customer", "customer", "customerId", "customer_id", "customerName", "customer_name");
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = model("Order", "market_order", "orderId", "order_id", "orderId", "order_id");
        Model orderItem = model("OrderItem", "market_order_item", "itemId", "item_id", "itemName", "item_name");
        orderItem.setOwner(order);
        orderItem.setProperties(List.of(orderItem.getIdProperty(), customerProperty));
        ViewItem customerItem = viewItem("Customer", "customer");
        customerItem.setSourceExpression("#.availableCustomers");
        View view = new View();
        view.setViewName("OrderItemEdit");
        view.setViewModel("OrderItem");
        view.setListItems(List.of(customerItem));
        IDynamicData owner = mock(IDynamicData.class);
        IDynamicData alice = dynamic("1001", "Alice");
        IDynamicData bob = dynamic("1002", "Bob");
        IDynamicData alina = dynamic("1003", "ALINA");
        when(owner.get("availableCustomers")).thenReturn(List.of(alice, bob, alina));
        when(daoService.getOneDetailByKey(View.class, "200")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "OrderItem")).thenReturn(orderItem);
        when(modelDataService.getDataList(eq("OrderItem"), any(IQueryFilter.class), eq(orderItem.getProperties())))
                .thenReturn(List.of(mock(IDynamicData.class)));
        when(modelDataService.getDataList(eq("Order"), any(IQueryFilter.class), eq(order.getProperties())))
                .thenReturn(List.of(owner));
        InputQueryRequest request = new InputQueryRequest();
        request.setViewId(200L);
        request.setViewItemId("Customer");
        request.setObjID("6001");
        request.setOwnerId("5001");
        request.setText("ali");

        InputQueryResult result = service.inputQuery(request);

        assertEquals(2, result.getItems().size());
        assertEquals("1001", result.getItems().get(0).getId());
        assertEquals("1003", result.getItems().get(1).getId());
        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataList(eq("Order"), filterCaptor.capture(), eq(order.getProperties()));
        assertEquals("`order_id`= ?", filterCaptor.getValue().generateSql().getSql());
        assertArrayEquals(new Object[]{"5001"}, filterCaptor.getValue().generateSql().getArgs());
        verify(modelDataService, never()).getDataListWithPageInfo(
                eq("Customer"),
                any(IQueryFilter.class),
                anyList(),
                any(PageNavigator.class),
                eq("customer_id"),
                eq(true));
    }

    private static ViewItem viewItem(String itemName, String modelProperty) {
        ViewItem item = new ViewItem();
        item.setItemName(itemName);
        item.setModelProperty(modelProperty);
        return item;
    }

    private static Property property(String name, String column) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        return property;
    }

    private static Model model(String name, String table, String idName, String idColumn, String showName, String showColumn) {
        Model model = new Model();
        model.setName(name);
        model.setTableName(table);
        Property id = property(idName, idColumn);
        Property show = property(showName, showColumn);
        model.setIdProperty(id);
        model.setShowProperty(show);
        model.setProperties(List.of(id, show));
        return model;
    }

    private static IDynamicData dynamic(String id, String text) {
        IDynamicData data = mock(IDynamicData.class);
        when(data.getId()).thenReturn(id);
        when(data.get("customerName")).thenReturn(text);
        return data;
    }
}
