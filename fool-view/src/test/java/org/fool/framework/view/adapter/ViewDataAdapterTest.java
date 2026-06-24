package org.fool.framework.view.adapter;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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

    private static ViewItem viewItem(String modelProperty, ItemEditType editType) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setEditType(editType);
        return item;
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
