package org.fool.framework.view.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageNavigatorResult;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;

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

    private static ViewItem viewItem(String modelProperty, ItemEditType editType) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setEditType(editType);
        return item;
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
