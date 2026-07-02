package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
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
        request.setViewName("OrderList");
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
