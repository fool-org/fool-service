package org.fool.framework.view.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageNavigatorResult;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.OperationViewParam;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.model.ViewOperationType;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ViewDataAdapterTest {
    @Test
    public void formatViewItemBecomesLegacyRowFmtAndIsExcludedFromValues() {
        View view = new View();
        ViewItem orderId = viewItem("orderId", ItemEditType.ReadOnly);
        orderId.setItemName("Order ID");
        ViewItem rowClass = viewItem("rowClass", ItemEditType.Format);
        view.setListItems(List.of(orderId, rowClass));

        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(new MapDynamicData("order-1", new LinkedHashMap<>(Map.of(
                "orderId", 1001,
                "rowClass", "warning")))));

        ListViewResult result = new ViewDataAdapter().getListViewResult(view, page);

        ListDataItem item = result.getItems().get(0);
        assertEquals("order-1", item.getId());
        assertEquals("warning", item.getRowFmt());
        assertEquals(1001, item.getValues().get("orderId"));
        assertFalse(item.getValues().containsKey("rowClass"));
        assertEquals(List.of("Order ID"), result.getCols());
        assertEquals(10, ItemEditType.Format.ordinal());
    }

    @Test
    public void listResultIncludesLegacyRefreshMetadata() {
        View view = new View();
        view.setAutoFreshInterval(30);
        view.setListItems(List.of(viewItem("orderId", ItemEditType.ReadOnly)));

        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(new MapDynamicData("order-1", new LinkedHashMap<>(Map.of("orderId", 1001)))));

        ListViewResult result = new ViewDataAdapter().getListViewResult(view, page);

        assertEquals(Integer.valueOf(30), result.getAutoFreshTime());
        assertNotNull(result.getFreshTime());
    }

    @Test
    public void listResultExcludesLegacyCollectionItems() {
        ViewItem symbol = viewItem("symbol", ItemEditType.ReadOnly);
        symbol.setItemName("Symbol");
        setProperty(symbol, property("symbol", PropertyType.String));

        Property itemsProperty = property("items", PropertyType.BusinessObject);
        itemsProperty.setIsCollection(true);
        ViewItem items = viewItem("items", ItemEditType.ReadOnly);
        items.setItemName("Items");
        setProperty(items, itemsProperty);

        View view = new View();
        view.setListItems(List.of(symbol, items));

        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(new MapDynamicData("order-1", new LinkedHashMap<>(Map.of(
                "symbol", "BTC-USDT",
                "items", List.of(new MapDynamicData("2001", new LinkedHashMap<>(Map.of("itemName", "Updated item")))))))));

        ListViewResult result = new ViewDataAdapter().getListViewResult(view, page);
        ListDataItem row = result.getItems().get(0);

        assertEquals(List.of("Symbol"), result.getCols());
        assertEquals("BTC-USDT", row.getValues().get("symbol"));
        assertFalse(row.getValues().containsKey("items"));
        assertEquals(1, row.getItems().size());
        assertEquals("symbol", row.getItems().get(0).getPrpId());
    }

    @Test
    public void listRowsExposeLegacyRowIndexFromPageOffset() {
        View view = new View();
        view.setListItems(List.of(viewItem("orderId", ItemEditType.ReadOnly)));

        PageNavigatorResult pageInfo = new PageNavigatorResult();
        pageInfo.setPageIndex(2);
        pageInfo.setPageSize(20);

        PageResult<IDynamicData> page = new PageResult<>();
        page.setPageInfo(pageInfo);
        page.setItems(List.of(new MapDynamicData("order-21", new LinkedHashMap<>(Map.of("orderId", 1021)))));

        ListViewResult result = new ViewDataAdapter().getListViewResult(view, page);

        ListDataItem item = result.getItems().get(0);
        Map<?, ?> serialized = new ObjectMapper().convertValue(item, Map.class);
        assertTrue(serialized.containsKey("rowIndex"));
        assertEquals(21L, ((Number) serialized.get("rowIndex")).longValue());
    }

    @Test
    public void listResultIncludesLegacyPagingAliasesAndDataAlias() {
        View view = new View();
        view.setListItems(List.of(viewItem("orderId", ItemEditType.ReadOnly)));

        PageNavigatorResult pageInfo = new PageNavigatorResult();
        pageInfo.setPageIndex(3);
        pageInfo.setPageSize(10);
        pageInfo.setTotal(25);
        pageInfo.setPageCount(3);

        PageResult<IDynamicData> page = new PageResult<>();
        page.setPageInfo(pageInfo);
        page.setItems(List.of(new MapDynamicData("order-21", new LinkedHashMap<>(Map.of("orderId", 1021)))));

        ListViewResult result = new ViewDataAdapter().getListViewResult(view, page);
        result.setFreshTime(null);

        Map<?, ?> serialized = new ObjectMapper().convertValue(result, Map.class);
        assertTrue("ListViewResult should expose legacy totalItem", serialized.containsKey("totalItem"));
        assertEquals(25L, ((Number) serialized.get("totalItem")).longValue());
        assertEquals(3L, ((Number) serialized.get("totalPage")).longValue());
        assertEquals(3L, ((Number) serialized.get("pageIndex")).longValue());
        assertEquals(serialized.get("items"), serialized.get("data"));
    }

    @Test
    public void listRowsExposeLegacyValueItems() {
        ViewItem orderId = viewItem("orderId", ItemEditType.ReadOnly);
        orderId.setItemName("Order ID");
        Property property = new Property();
        property.setName("orderId");
        property.setPropertyType(PropertyType.Long);
        setProperty(orderId, property);

        View view = new View();
        view.setListItems(List.of(orderId));

        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(new MapDynamicData("order-1", new LinkedHashMap<>(Map.of("orderId", 1001)))));

        ListDataItem row = new ViewDataAdapter().getListViewResult(view, page).getItems().get(0);

        Map<?, ?> serialized = new ObjectMapper().convertValue(row, Map.class);
        assertTrue("ListDataItem should expose legacy Items", serialized.containsKey("items"));
        List<?> legacyItems = (List<?>) serialized.get("items");
        Map<?, ?> value = (Map<?, ?>) legacyItems.get(0);
        assertEquals("1001", value.get("objId"));
        assertEquals("orderId", value.get("prpId"));
        assertEquals("1001", value.get("fmtValue"));
        assertEquals("Order ID", value.get("prpShowName"));
        assertEquals("Long", value.get("prpType"));
        assertEquals(0, ((Number) value.get("prpModelId")).longValue());
        assertEquals(true, value.get("readOnly"));
        assertEquals("ReadOnly", value.get("editType"));
    }

    @Test
    public void listRowsFormatLegacyTypedValueItems() {
        ViewItem tradeDate = viewItem("tradeDate", ItemEditType.ReadOnly);
        tradeDate.setItemName("Trade Date");
        setProperty(tradeDate, property("tradeDate", PropertyType.Date));

        ViewItem tradeTime = viewItem("tradeTime", ItemEditType.ReadOnly);
        tradeTime.setItemName("Trade Time");
        setProperty(tradeTime, property("tradeTime", PropertyType.Time));

        Model stateModel = new Model();
        stateModel.setEnumValues(List.of(enumValue("Open", "1"), enumValue("Closed", "2")));
        Property stateProperty = property("state", PropertyType.Enum);
        stateProperty.setPropertyModel(stateModel);
        ViewItem state = viewItem("state", ItemEditType.ReadOnly);
        state.setItemName("State");
        setProperty(state, stateProperty);

        Property customerId = property("customerId", PropertyType.Long);
        Property customerName = property("name", PropertyType.String);
        Model customerModel = new Model();
        customerModel.setIdProperty(customerId);
        customerModel.setProperties(List.of(customerId, customerName));
        Property customerProperty = property("customer", PropertyType.BusinessObject);
        customerProperty.setPropertyModel(customerModel);
        ViewItem customer = viewItem("customer", ItemEditType.ReadOnly);
        customer.setItemName("Customer");
        setProperty(customer, customerProperty);

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("tradeDate", LocalDate.of(2026, 7, 3));
        values.put("tradeTime", LocalTime.of(9, 5, 6));
        values.put("state", 2);
        values.put("customer", new MapDynamicData("C-7", new LinkedHashMap<>(Map.of("name", "Alice"))));

        View view = new View();
        view.setListItems(List.of(tradeDate, tradeTime, state, customer));
        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(new MapDynamicData("order-1", values)));

        ListDataItem row = new ViewDataAdapter().getListViewResult(view, page).getItems().get(0);
        List<ListDataValue> items = row.getItems();

        assertEquals("2026-07-03", items.get(0).getFmtValue());
        assertEquals("09:05:06", items.get(1).getFmtValue());
        assertEquals("2", items.get(2).getObjId());
        assertEquals("Closed", items.get(2).getFmtValue());
        assertEquals("C-7", items.get(3).getObjId());
        assertEquals("Alice", items.get(3).getFmtValue());
        assertEquals("Alice", row.getValues().get("customer"));
    }

    @Test
    public void listRowsAndColumnsFollowLegacyShowIndexOrder() {
        ViewItem symbol = viewItem("symbol", ItemEditType.ReadOnly);
        symbol.setItemName("Symbol");
        setShowIndex(symbol, 2);
        ViewItem orderId = viewItem("orderId", ItemEditType.ReadOnly);
        orderId.setItemName("Order ID");
        setShowIndex(orderId, 1);

        View view = new View();
        view.setListItems(List.of(symbol, orderId));

        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(new MapDynamicData("order-1", new LinkedHashMap<>(Map.of(
                "orderId", 1001,
                "symbol", "BTC-USDT")))));

        ListViewResult result = new ViewDataAdapter().getListViewResult(view, page);

        assertEquals(List.of("Order ID", "Symbol"), result.getCols());
        assertEquals(List.of("orderId", "symbol"), new ArrayList<>(result.getItems().get(0).getValues().keySet()));
    }

    @Test
    public void detailResultIncludesLegacySimpleDataAndOperations() {
        ViewItem orderId = viewItem("orderId", ItemEditType.ReadOnly);
        orderId.setItemName("Order ID");
        setProperty(orderId, property("orderId", PropertyType.Long));
        ViewItem symbol = viewItem("symbol", ItemEditType.ReadOnly);
        symbol.setItemName("Symbol");
        setProperty(symbol, property("symbol", PropertyType.String));
        View detailView = new View();
        detailView.setId(200L);
        detailView.setViewName("OrderDetail");
        ViewOperation edit = new ViewOperation();
        edit.setName("Edit");
        edit.setType(ViewOperationType.MODAL_DETAIL_VIEW);
        edit.setResultView(detailView);
        Operation operation = new Operation();
        operation.setId(300L);
        edit.setOperation(operation);
        OperationViewParam param = new OperationViewParam();
        param.setName("审批意见");
        param.setIndex(1);
        param.setParamId(7201L);
        edit.setParams(List.of(param));

        View view = new View();
        view.setViewName("OrderDetail");
        view.setViewModel("Order");
        view.setAutoFreshInterval(15);
        view.setListItems(List.of(orderId, symbol));
        view.setOperations(List.of(edit));
        MapDynamicData data = new MapDynamicData("1001", new LinkedHashMap<>(Map.of(
                "orderId", 1001,
                "symbol", "BTC-USDT")));

        QueryDataDetailResult result = new ViewDataAdapter().getDetailViewResult(view, data);

        assertEquals(Integer.valueOf(15), result.getAutoFreshTime());
        assertEquals(Boolean.TRUE, result.getCanEdit());
        assertEquals("1001", result.getData().getObjId());
        assertEquals("OrderDetail", result.getData().getName());
        assertEquals("Order", result.getData().getModel());
        assertEquals(2, result.getData().getSimpleData().size());
        assertEquals("orderId", result.getData().getSimpleData().get(0).getPrpId());
        assertEquals("1001", result.getData().getSimpleData().get(0).getFmtValue());
        assertEquals("Symbol", result.getData().getSimpleData().get(1).getPrpShowName());
        assertEquals("BTC-USDT", result.getData().getSimpleData().get(1).getFmtValue());
        assertTrue(result.getData().getItems().isEmpty());
        assertEquals(1, result.getOperations().size());
        assertEquals("Edit", result.getOperations().get(0).getName());
        assertEquals(Long.valueOf(300L), result.getOperations().get(0).getId());
        assertEquals(Long.valueOf(200L), result.getOperations().get(0).getViewId());
        assertEquals("审批意见", result.getOperations().get(0).getParams().get(0).getName());
        assertEquals(Integer.valueOf(1), result.getOperations().get(0).getParams().get(0).getIndex());
        assertEquals(Long.valueOf(7201L), result.getOperations().get(0).getParams().get(0).getParamId());
    }

    @Test
    public void detailResultIncludesLegacyCollectionItems() {
        Property itemId = property("itemId", PropertyType.Long);
        itemId.setRemark("Item ID");
        Property itemName = property("itemName", PropertyType.String);
        itemName.setRemark("Item Name");
        Model itemModel = new Model();
        itemModel.setId(101L);
        itemModel.setName("OrderItem");
        itemModel.setIdProperty(itemId);
        itemModel.setProperties(List.of(itemId, itemName));

        Property itemsProperty = property("items", PropertyType.BusinessObject);
        itemsProperty.setIsCollection(true);
        itemsProperty.setPropertyModel(itemModel);
        ViewItem items = viewItem("items", ItemEditType.ReadOnly);
        items.setItemName("Items");
        setProperty(items, itemsProperty);

        ViewItem symbol = viewItem("symbol", ItemEditType.ReadOnly);
        symbol.setItemName("Symbol");
        setProperty(symbol, property("symbol", PropertyType.String));

        View view = new View();
        view.setViewName("OrderDetail");
        view.setViewModel("Order");
        view.setListItems(List.of(symbol, items));

        Map<String, Object> childValues = new LinkedHashMap<>();
        childValues.put("itemId", 2001L);
        childValues.put("itemName", "Updated item");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("symbol", "BTC-USDT");
        values.put("items", List.of(new MapDynamicData("2001", childValues)));

        QueryDataDetailResult result = new ViewDataAdapter().getDetailViewResult(
                view,
                new MapDynamicData("1001", values));

        assertEquals(1, result.getData().getSimpleData().size());
        assertEquals("symbol", result.getData().getSimpleData().get(0).getPrpId());
        assertEquals(1, result.getData().getItems().size());
        QueryDataDetailResult.PropertyDataItems group = result.getData().getItems().get(0);
        assertEquals("OrderItem", group.getName());
        assertEquals("items", group.getPrpId());
        assertEquals("Items", group.getItemName());
        assertEquals(2, group.getProperties().size());
        assertEquals("Item Name", group.getProperties().get(1).getPrpShowName());
        assertEquals(1, group.getItems().size());
        assertEquals("2001", group.getItems().get(0).getDataId());
        assertEquals("Updated item", group.getItems().get(0).getValues().get(1).getFmtValue());
    }

    @Test
    public void detailCollectionItemsExposeConfiguredLegacyChildViews() {
        Property itemId = property("itemId", PropertyType.Long);
        Model itemModel = new Model();
        itemModel.setName("OrderItem");
        itemModel.setIdProperty(itemId);
        itemModel.setProperties(List.of(itemId));

        Property itemsProperty = property("items", PropertyType.BusinessObject);
        itemsProperty.setIsCollection(true);
        itemsProperty.setPropertyModel(itemModel);
        ViewItem items = viewItem("items", ItemEditType.ReadOnly);
        items.setItemName("Items");
        items.setListViewId(101L);
        items.setEditViewId(102L);
        items.setSelectedViewId(103L);
        setProperty(items, itemsProperty);

        View view = new View();
        view.setViewName("OrderDetail");
        view.setViewModel("Order");
        view.setListItems(List.of(items));

        QueryDataDetailResult result = new ViewDataAdapter().getDetailViewResult(
                view,
                new MapDynamicData("1001", new LinkedHashMap<>(Map.of("items", List.of()))));

        QueryDataDetailResult.PropertyDataItems group = result.getData().getItems().get(0);
        assertEquals(Long.valueOf(101L), group.getListViewId());
        assertEquals(Long.valueOf(102L), group.getDetailViewId());
        assertEquals(Long.valueOf(103L), group.getSelectedView());
        assertEquals(Boolean.TRUE, group.getSelectFromExists());
    }

    private static ViewItem viewItem(String modelProperty, ItemEditType editType) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setEditType(editType);
        return item;
    }

    private static Property property(String name, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setPropertyType(type);
        return property;
    }

    private static EnumValue enumValue(String name, String value) {
        EnumValue enumValue = new EnumValue();
        enumValue.setName(name);
        enumValue.setValue(value);
        return enumValue;
    }

    private static void setShowIndex(ViewItem item, int showIndex) {
        try {
            ViewItem.class.getMethod("setShowIndex", Integer.class).invoke(item, showIndex);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy showIndex metadata");
        }
    }

    private static void setProperty(ViewItem item, Property property) {
        try {
            ViewItem.class.getMethod("setProperty", Property.class).invoke(item, property);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy property metadata");
        }
    }

    private record MapDynamicData(String id, Map<String, Object> values) implements IDynamicData {
        @Override
        public Object get(String field) {
            return values.get(field);
        }

        @Override
        public void set(String field, Object value) {
            values.put(field, value);
        }

        @Override
        public Object invokeWithReturn(String methodName, Object... args) {
            return null;
        }

        @Override
        public void invoke(String methodName, Object... args) {
        }

        @Override
        public Map<String, Object> toMap() {
            return new LinkedHashMap<>(values);
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
