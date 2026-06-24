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
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

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

    private static ViewItem viewItem(String modelProperty, int showIndex) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setShowIndex(showIndex);
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
