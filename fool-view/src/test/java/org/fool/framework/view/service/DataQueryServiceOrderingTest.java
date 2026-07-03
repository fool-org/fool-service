package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryValue;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataQueryServiceOrderingTest {

    @Test
    public void queryViewDataListOrdersByFirstLegacyShowIndexItemDescending() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(
                viewItem("symbol", 20),
                viewItem("orderId", 10)));
        Model model = model("Order", List.of(
                property("orderId", "order_id"),
                property("symbol", "order_symbol")));
        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageIndex(1);
        pageNavigator.setPageSize(10);
        PageResult<IDynamicData> pageResult = new PageResult<>();
        ListViewResult expected = new ListViewResult();
        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(view, pageResult)).thenReturn(expected);

        ListViewResult actual = service.queryViewDataList("OrderList", null, pageNavigator);

        assertSame(expected, actual);
        ArgumentCaptor<List<Property>> propertiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                propertiesCaptor.capture(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true));
        assertEquals(
                List.of("orderId", "symbol"),
                propertiesCaptor.getValue().stream().map(Property::getName).toList());
    }

    @Test
    public void queryViewDataListOrdersBusinessObjectByLegacyShowProperty() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(
                viewItem("customer", 10),
                viewItem("orderId", 20)));
        Model customer = model("Customer", List.of(
                property("customerId", "customer_id"),
                property("customerName", "customer_name")));
        customer.setTableName("customer");
        customer.setIdProperty(customer.getProperties().get(0));
        customer.setShowProperty(customer.getProperties().get(1));
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = model("Order", List.of(customerProperty, property("orderId", "order_id")));
        PageNavigator pageNavigator = new PageNavigator();
        PageResult<IDynamicData> pageResult = new PageResult<>();
        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("`customer`.`customer_name`"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(eq(view), eq(pageResult))).thenReturn(new ListViewResult());

        service.queryViewDataList("OrderList", null, pageNavigator);

        verify(modelDataService).getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("`customer`.`customer_name`"),
                eq(true));
    }

    @Test
    public void queryViewDataListAppliesLegacyViewFilterBeforeRequestFilter() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("OpenOrderList");
        view.setViewModel("Order");
        view.setFilter("`order_state`='OPEN'");
        view.setListItems(List.of(viewItem("orderId", 10), viewItem("symbol", 20)));
        Model model = model("Order", List.of(
                property("orderId", "order_id"),
                property("symbol", "order_symbol")));
        QueryValue symbolFilter = new QueryValue();
        symbolFilter.setValue("BTC-USDT");
        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageIndex(1);
        pageNavigator.setPageSize(10);
        PageResult<IDynamicData> pageResult = new PageResult<>();
        when(daoService.getOneDetailByKey(View.class, "OpenOrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(eq(view), eq(pageResult))).thenReturn(new ListViewResult());

        service.queryViewDataList("OpenOrderList", Map.of("symbol", symbolFilter), pageNavigator);

        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Order"),
                filterCaptor.capture(),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true));
        var sql = filterCaptor.getValue().generateSql();
        assertEquals("(`order_state`='OPEN') And (`order_symbol`= ?)", sql.getSql());
        assertArrayEquals(new Object[]{"BTC-USDT"}, sql.getArgs());
    }

    @Test
    public void queryLegacyViewDataAppliesLegacyQueryFilterAfterViewFilter() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("OpenOrderList");
        view.setViewModel("Order");
        view.setFilter("`tenant_id`=1");
        view.setListItems(List.of(viewItem("orderId", 10), viewItem("symbol", 20)));
        Model model = model("Order", List.of(
                property("orderId", "order_id"),
                property("symbol", "order_symbol")));
        PageNavigator pageNavigator = new PageNavigator();
        PageResult<IDynamicData> pageResult = new PageResult<>();
        when(daoService.getOneDetailByKey(View.class, "42")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(eq(view), eq(pageResult))).thenReturn(new ListViewResult());

        service.queryLegacyViewData("42", pageNavigator, "`order_state`='OPEN'");

        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Order"),
                filterCaptor.capture(),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true));
        var sql = filterCaptor.getValue().generateSql();
        assertEquals("(`tenant_id`=1) And (`order_state`='OPEN')", sql.getSql());
        assertArrayEquals(new Object[]{}, sql.getArgs());
    }

    @Test
    public void queryLegacyViewDataAppliesKeywordThroughViewMetadata() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("CandidateList");
        view.setViewModel("Candidate");
        view.setListItems(List.of(
                viewItem("candidateId", 10, InputType.TEXT_BOX, true),
                viewItem("displayName", 20, InputType.READ_ONLY, false)));
        Model model = model("Candidate", List.of(
                property("candidateId", "candidate_id"),
                property("displayName", "display_name")));
        PageNavigator pageNavigator = new PageNavigator();
        PageResult<IDynamicData> pageResult = new PageResult<>();
        when(daoService.getOneDetailByKey(View.class, "101")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Candidate")).thenReturn(model);
        when(modelDataService.getDataListWithPageInfo(
                eq("Candidate"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("candidate_id"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(eq(view), eq(pageResult))).thenReturn(new ListViewResult());

        service.queryLegacyViewData("101", pageNavigator, null, "Ada");

        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Candidate"),
                filterCaptor.capture(),
                anyList(),
                eq(pageNavigator),
                eq("candidate_id"),
                eq(true));
        var sql = filterCaptor.getValue().generateSql();
        assertEquals("( 1=1 ) And (`display_name` LIKE ?)", sql.getSql());
        assertArrayEquals(new Object[]{"%Ada%"}, sql.getArgs());
    }

    @Test
    public void queryViewDataListAppliesLegacyKeywordToReadOnlyItems() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(
                viewItem("orderId", 10, InputType.TEXT_BOX, true),
                viewItem("symbol", 20, InputType.READ_ONLY, false),
                viewItem("state", 30, InputType.READ_ONLY, false)));
        Model model = model("Order", List.of(
                property("orderId", "order_id"),
                property("symbol", "order_symbol"),
                property("state", "order_state")));
        PageNavigator pageNavigator = new PageNavigator();
        PageResult<IDynamicData> pageResult = new PageResult<>();
        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(eq(view), eq(pageResult))).thenReturn(new ListViewResult());

        service.queryViewDataList("OrderList", null, pageNavigator, "USDT");

        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Order"),
                filterCaptor.capture(),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true));
        var sql = filterCaptor.getValue().generateSql();
        assertEquals("( 1=1 ) And (`order_symbol` LIKE ? OR `order_state` LIKE ?)", sql.getSql());
        assertArrayEquals(new Object[]{"%USDT%", "%USDT%"}, sql.getArgs());
    }

    @Test
    public void queryViewDataListAppliesLegacyKeywordToBusinessObjectShowProperty() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewName("OrderList");
        view.setViewModel("Order");
        view.setListItems(List.of(
                viewItem("orderId", 10, InputType.TEXT_BOX, true),
                viewItem("customer", 20, InputType.READ_ONLY, false)));
        Model customer = model("Customer", List.of(
                property("customerId", "customer_id"),
                property("customerName", "customer_name")));
        customer.setTableName("customer");
        customer.setIdProperty(customer.getProperties().get(0));
        customer.setShowProperty(customer.getProperties().get(1));
        Property customerProperty = property("customer", "customer_id");
        customerProperty.setPropertyType(PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customer);
        Model order = model("Order", List.of(
                property("orderId", "order_id"),
                customerProperty));
        PageNavigator pageNavigator = new PageNavigator();
        PageResult<IDynamicData> pageResult = new PageResult<>();
        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"),
                any(IQueryFilter.class),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true)))
                .thenReturn(pageResult);
        when(viewAdapter.getListViewResult(eq(view), eq(pageResult))).thenReturn(new ListViewResult());

        service.queryViewDataList("OrderList", null, pageNavigator, "Ada");

        ArgumentCaptor<IQueryFilter> filterCaptor = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(modelDataService).getDataListWithPageInfo(
                eq("Order"),
                filterCaptor.capture(),
                anyList(),
                eq(pageNavigator),
                eq("order_id"),
                eq(true));
        var sql = filterCaptor.getValue().generateSql();
        assertEquals("( 1=1 ) And (`customer`.`customer_name` LIKE ?)", sql.getSql());
        assertArrayEquals(new Object[]{"%Ada%"}, sql.getArgs());
    }

    private static ViewItem viewItem(String modelProperty, int showIndex) {
        return viewItem(modelProperty, showIndex, InputType.READ_ONLY, false);
    }

    private static ViewItem viewItem(String modelProperty, int showIndex, InputType inputType, boolean canEdit) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setShowIndex(showIndex);
        item.setInputType(inputType);
        item.setCanEdit(canEdit);
        return item;
    }

    private static Model model(String name, List<Property> properties) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(ModelType.DYNAMIC);
        model.setTableName("market_order");
        model.setProperties(properties);
        return model;
    }

    private static Property property(String name, String column) {
        Property property = new Property();
        property.setName(name);
        property.setRemark(name);
        property.setColumn(column);
        property.setPropertyType(PropertyType.String);
        property.setIsCollection(false);
        property.setAllowDbNull(false);
        property.setCheck(false);
        return property;
    }
}
