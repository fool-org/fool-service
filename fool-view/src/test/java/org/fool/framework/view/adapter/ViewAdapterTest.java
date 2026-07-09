package org.fool.framework.view.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.PropertyType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.ReadItemViewInfo;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.OperationViewParam;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.model.ViewOperationType;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

public class ViewAdapterTest {
    @Test
    public void readItemViewIncludesLegacySimpleItems() {
        Property orderIdProperty = new Property();
        orderIdProperty.setName("orderId");
        orderIdProperty.setPropertyType(PropertyType.Long);

        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.TextBox);
        orderId.setId(901L);
        orderId.setItemName("Order ID");
        orderId.setCanEdit(false);
        setShowIndex(orderId, 1);
        setProperty(orderId, orderIdProperty);

        View view = new View();
        view.setId(100L);
        view.setViewName("OrderDetail");
        view.setListItems(List.of(orderId));

        ReadItemViewInfo info = new ViewAdapter().getReadItemView(view);

        assertEquals(Long.valueOf(100L), info.getViewId());
        assertEquals("OrderDetail", info.getViewName());
        assertEquals(1, info.getItems().size());
        assertEquals("Order ID", info.getItems().get(0).getName());
        assertEquals(Integer.valueOf(1), info.getItems().get(0).getIndex());
        assertEquals(PropertyType.Long, info.getItems().get(0).getPrpType());
        assertEquals("orderId", info.getItems().get(0).getPrpId());
        assertEquals("901", info.getItems().get(0).getId());
        assertEquals("Order ID", info.getItems().get(0).getPrpShowName());
        assertTrue(info.getItems().get(0).isReadOnly());
        assertEquals(ItemEditType.TextBox, info.getItems().get(0).getEditType());
        assertEquals(0, info.getDetailViews().size());
    }

    @Test
    public void readItemViewSerializesLegacyPascalMetadata() {
        Property orderIdProperty = new Property();
        orderIdProperty.setName("orderId");
        orderIdProperty.setPropertyType(PropertyType.Long);

        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        orderId.setId(901L);
        orderId.setItemName("Order ID");
        setShowIndex(orderId, 1);
        setProperty(orderId, orderIdProperty);

        View view = new View();
        view.setId(102L);
        view.setViewName("OrderDetail");
        view.setListItems(List.of(orderId));

        Map<?, ?> serialized = new ObjectMapper().convertValue(new ViewAdapter().getReadItemView(view), Map.class);

        assertEquals("OrderDetail", serialized.get("ViewName"));
        assertEquals(102L, ((Number) serialized.get("ViewId")).longValue());

        List<?> items = (List<?>) serialized.get("Items");
        assertEquals(serialized.get("items"), items);
        Map<?, ?> item = (Map<?, ?>) items.get(0);
        assertEquals("Order ID", item.get("Name"));
        assertEquals("Long", String.valueOf(item.get("PrpType")));
        assertEquals(1, ((Number) item.get("Index")).intValue());
        assertEquals("orderId", item.get("PrpId"));
        assertEquals(0L, ((Number) item.get("PrpModelId")).longValue());
        assertEquals("901", item.get("ID"));
        assertEquals("Order ID", item.get("PrpShowName"));
        assertEquals(true, item.get("ReadOnly"));
        assertEquals("ReadOnly", String.valueOf(item.get("EditType")));
        assertEquals(serialized.get("detailViews"), serialized.get("DetailViews"));
    }

    @Test
    public void readItemViewUsesModelPropertyWhenPropertyIsNotHydrated() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);

        View view = new View();
        view.setListItems(List.of(orderId));

        ReadItemViewInfo info = new ViewAdapter().getReadItemView(view);

        assertEquals("orderId", info.getItems().get(0).getPrpId());
    }

    @Test
    public void readItemViewIncludesCollectionEditViewDetailItems() {
        Property itemsProperty = new Property();
        itemsProperty.setName("items");
        itemsProperty.setPropertyType(PropertyType.BusinessObject);
        itemsProperty.setIsCollection(true);
        ViewItem items = viewItem("items", "Items", ItemEditType.TextBox);
        items.setItemName("Items");
        items.setEditViewId(102L);
        setProperty(items, itemsProperty);

        Property itemNameProperty = new Property();
        itemNameProperty.setName("itemName");
        itemNameProperty.setPropertyType(PropertyType.String);
        ViewItem itemName = viewItem("itemName", "Item Name", ItemEditType.TextBox);
        itemName.setItemName("Item Name");
        setProperty(itemName, itemNameProperty);

        View editView = new View();
        editView.setId(102L);
        editView.setListItems(List.of(itemName));

        View view = new View();
        view.setListItems(List.of(items));

        ReadItemViewInfo info = new ViewAdapter().getReadItemView(view, id -> id.equals(102L) ? editView : null);

        assertEquals(0, info.getItems().size());
        assertEquals(1, info.getDetailViews().size());
        assertEquals("items", info.getDetailViews().get(0).getPrpId());
        assertEquals("Items", info.getDetailViews().get(0).getName());
        assertEquals(1, info.getDetailViews().get(0).getItems().size());
        assertEquals("itemName", info.getDetailViews().get(0).getItems().get(0).getPrpId());
        assertEquals("Item Name", info.getDetailViews().get(0).getItems().get(0).getPrpShowName());
    }

    @Test
    public void formatViewItemsAreExcludedFromLegacyListColumnsAndInputs() {
        View view = new View();
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        ViewItem rowClass = viewItem("rowClass", "Row Class", ItemEditType.Format);
        view.setListItems(List.of(orderId, rowClass));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(1, info.getTableColumn().size());
        assertEquals("orderId", info.getTableColumn().get(0).getProperty());
        assertEquals(0, info.getInputInfo().size());
    }

    @Test
    public void collectionViewItemsAreExcludedFromLegacyListColumnsAndInputs() {
        ViewItem symbol = viewItem("symbol", "Symbol", ItemEditType.ReadOnly);
        Property symbolProperty = new Property();
        symbolProperty.setName("symbol");
        symbolProperty.setPropertyType(PropertyType.String);
        setProperty(symbol, symbolProperty);

        ViewItem items = viewItem("items", "Items", ItemEditType.TextBox);
        items.setInputType(InputType.TEXT_BOX);
        Property itemsProperty = new Property();
        itemsProperty.setName("items");
        itemsProperty.setPropertyType(PropertyType.BusinessObject);
        itemsProperty.setIsCollection(true);
        setProperty(items, itemsProperty);

        View view = new View();
        view.setListItems(List.of(symbol, items));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(1, info.getTableColumn().size());
        assertEquals("symbol", info.getTableColumn().get(0).getProperty());
        assertEquals(0, info.getInputInfo().size());
    }

    @Test
    public void viewOperationsAreMappedToLegacyOperationInfo() {
        View detailView = new View();
        detailView.setId(200L);
        detailView.setViewName("OrderDetail");

        ViewOperation create = operation("Create", false, ViewOperationType.MODAL_DETAIL_VIEW, detailView);
        create.setLocation(0);
        create.setOperation(operation(300L));
        OperationViewParam param = new OperationViewParam();
        param.setName("审批意见");
        param.setIndex(1);
        param.setParamId(7201L);
        param.setParamName("remark");
        param.setViewId(201L);
        create.setParams(List.of(param));
        ViewOperation delete = operation("Delete", true, ViewOperationType.COMMAND, null);
        delete.setLocation(2);

        View view = new View();
        view.setListItems(List.of());
        view.setOperations(List.of(create, delete));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(2, info.getOperations().size());
        assertEquals("Create", info.getOperations().get(0).getText());
        assertEquals("Create", legacyOperationName(info.getOperations().get(0)));
        assertFalse(info.getOperations().get(0).isRequireSelect());
        assertEquals(ViewOperationType.MODAL_DETAIL_VIEW, info.getOperations().get(0).getType());
        assertEquals(0, info.getOperations().get(0).getLocation());
        assertEquals("OrderDetail", info.getOperations().get(0).getViewName());
        assertEquals(Long.valueOf(200L), info.getOperations().get(0).getViewId());
        assertEquals(Long.valueOf(300L), info.getOperations().get(0).getId());
        assertEquals(1, info.getOperations().get(0).getParams().size());
        assertEquals("审批意见", info.getOperations().get(0).getParams().get(0).getName());
        assertEquals(Integer.valueOf(1), info.getOperations().get(0).getParams().get(0).getIndex());
        assertEquals(Long.valueOf(7201L), info.getOperations().get(0).getParams().get(0).getParamId());
        assertEquals("remark", info.getOperations().get(0).getParams().get(0).getParamName());
        assertEquals(Long.valueOf(201L), info.getOperations().get(0).getParams().get(0).getViewId());

        assertEquals("Delete", info.getOperations().get(1).getText());
        assertTrue(info.getOperations().get(1).isRequireSelect());
        assertEquals(ViewOperationType.COMMAND, info.getOperations().get(1).getType());
        assertEquals(2, info.getOperations().get(1).getLocation());
        assertEquals(Long.valueOf(0L), info.getOperations().get(1).getId());
    }

    @Test
    public void viewInfoIncludesLegacyAutoFreshTime() {
        View view = new View();
        view.setAutoFreshInterval(45);
        view.setListItems(List.of());

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(Integer.valueOf(45), info.getAutoFreshTime());
    }

    @Test
    public void viewInfoIncludesLegacyDetailViewId() {
        View detail = new View();
        detail.setId(500L);

        View view = new View();
        view.setDefaultDetailView(detail);
        view.setListItems(List.of());

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(Long.valueOf(500L), detailViewId(info));
    }

    @Test
    public void viewInfoIncludesLegacyEmptyTempFile() {
        View view = new View();
        view.setListItems(List.of());

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("", legacyTempFile(info));
    }

    @Test
    public void viewInfoIncludesLegacyTempFile() {
        View view = new View();
        view.setTempFile("viewWithChart");
        view.setListItems(List.of());

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("viewWithChart", legacyTempFile(info));
    }

    @Test
    public void viewInfoIncludesLegacyNameAndShowType() {
        View view = new View();
        view.setViewName("OrderList");
        view.setViewType(org.fool.framework.view.model.ViewType.ListView);
        view.setListItems(List.of());

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("OrderList", legacyName(info));
        assertEquals(org.fool.framework.view.model.ViewType.ListView, legacyType(info));
        assertEquals(org.fool.framework.view.model.ViewType.ListView, legacyShowType(info));
    }

    @Test
    public void viewInfoSerializesLegacyPascalMetadata() {
        View detail = new View();
        detail.setId(500L);
        detail.setViewName("OrderDetail");

        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        orderId.setId(901L);
        orderId.setItemName("Order ID");
        orderId.setFormatRegx("id-format");
        orderId.setCanEdit(false);
        setShowIndex(orderId, 1);
        setWidth(orderId, 180);
        Property property = new Property();
        property.setName("orderId");
        property.setPropertyType(PropertyType.Long);
        setProperty(orderId, property);

        ViewOperation open = operation("Open", true, ViewOperationType.DETAIL_VIEW, detail);
        open.setOperation(operation(300L));

        View view = new View();
        view.setId(100L);
        view.setViewName("OrderList");
        view.setViewType(org.fool.framework.view.model.ViewType.ListView);
        view.setDefaultDetailView(detail);
        view.setAutoFreshInterval(45);
        view.setListItems(List.of(orderId));
        view.setOperations(List.of(open));

        Map<?, ?> serialized = new ObjectMapper().convertValue(new ViewAdapter().getViewInfo(view), Map.class);

        assertEquals(100L, ((Number) serialized.get("ID")).longValue());
        assertEquals(100L, ((Number) serialized.get("ViewId")).longValue());
        assertEquals("OrderList", serialized.get("Name"));
        assertEquals("ListView", String.valueOf(serialized.get("Type")));
        assertEquals(500L, ((Number) serialized.get("DetailViewId")).longValue());
        assertEquals("", serialized.get("TempFile"));
        assertEquals("ListView", String.valueOf(serialized.get("ShowType")));
        assertEquals(45, ((Number) serialized.get("AutoFreshTime")).intValue());

        List<?> items = (List<?>) serialized.get("Items");
        assertEquals(serialized.get("tableColumn"), items);
        Map<?, ?> column = (Map<?, ?>) items.get(0);
        assertEquals(901L, ((Number) column.get("ID")).longValue());
        assertEquals("Order ID", column.get("Name"));
        assertEquals("id-format", column.get("Format"));
        assertEquals(true, column.get("IsReadOnly"));
        assertEquals(1, ((Number) column.get("ShowIndex")).intValue());
        assertEquals(180, ((Number) column.get("Width")).intValue());
        assertEquals("orderId", column.get("PropertyName"));
        assertEquals(0L, ((Number) column.get("PropertyId")).longValue());
        assertEquals(0, ((Number) column.get("ListViewType")).intValue());
        assertEquals(0L, ((Number) column.get("ListViewId")).longValue());
        assertEquals(0L, ((Number) column.get("EditViewId")).longValue());
        assertEquals(0L, ((Number) column.get("EditExp")).longValue());
        assertEquals("Long", String.valueOf(column.get("PropertyType")));
        assertEquals(0L, ((Number) column.get("PropertyModel")).longValue());
        assertEquals("ReadOnly", String.valueOf(column.get("EditType")));

        List<?> operations = (List<?>) serialized.get("Operations");
        assertEquals(serialized.get("operations"), operations);
        Map<?, ?> operation = (Map<?, ?>) operations.get(0);
        assertEquals(300L, ((Number) operation.get("ID")).longValue());
        assertEquals("Open", operation.get("Name"));
        assertEquals(true, operation.get("RequireSelect"));
        assertEquals(500L, ((Number) operation.get("ViewID")).longValue());
    }

    @Test
    public void viewInfoOrdersLegacyListColumnsByShowIndex() {
        ViewItem symbol = viewItem("symbol", "Symbol", ItemEditType.ReadOnly);
        setShowIndex(symbol, 2);
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        setShowIndex(orderId, 1);

        View view = new View();
        view.setListItems(List.of(symbol, orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("orderId", info.getTableColumn().get(0).getProperty());
        assertEquals("symbol", info.getTableColumn().get(1).getProperty());
    }

    @Test
    public void viewInfoIncludesLegacyColumnWidth() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        setWidth(orderId, 180);

        View view = new View();
        view.setListItems(List.of(orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(Integer.valueOf(180), columnWidth(info.getTableColumn().get(0)));
    }

    @Test
    public void viewInfoIncludesLegacyColumnName() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        orderId.setItemName("OrderName");

        View view = new View();
        view.setListItems(List.of(orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("OrderName", columnName(info.getTableColumn().get(0)));
    }

    @Test
    public void viewInfoIncludesLegacyColumnId() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        orderId.setId(901L);

        View view = new View();
        view.setListItems(List.of(orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(Long.valueOf(901L), columnId(info.getTableColumn().get(0)));
    }

    @Test
    public void viewInfoIncludesLegacyColumnPropertyName() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        Property property = new Property();
        property.setName("orderId");
        setProperty(orderId, property);

        View view = new View();
        view.setListItems(List.of(orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("orderId", columnPropertyName(info.getTableColumn().get(0)));
    }

    @Test
    public void viewInfoUsesEmptyLegacyColumnPropertyNameWhenPropertyIsMissing() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);

        View view = new View();
        view.setListItems(List.of(orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals("", columnPropertyName(info.getTableColumn().get(0)));
    }

    @Test
    public void viewInfoIncludesLegacyColumnBehaviorMetadata() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.TextBox);
        orderId.setFormatRegx("format-price");
        orderId.setCanEdit(false);

        View view = new View();
        view.setListItems(List.of(orderId));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        Object column = info.getTableColumn().get(0);
        assertEquals("format-price", columnFormat(column));
        assertEquals(Boolean.TRUE, columnIsReadOnly(column));
        assertEquals(ItemEditType.TextBox, columnEditType(column));
    }

    @Test
    public void viewInfoIncludesLegacyLinkedListViewDefaults() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);

        View view = new View();
        view.setListItems(List.of(orderId));

        Object column = new ViewAdapter().getViewInfo(view).getTableColumn().get(0);

        assertEquals(Long.valueOf(0L), columnListViewId(column));
        assertEquals(Integer.valueOf(0), columnListViewType(column));
    }

    @Test
    public void viewInfoIncludesConfiguredLegacyLinkedViewIds() {
        ViewItem customer = viewItem("customer", "Customer", ItemEditType.TextBox);
        customer.setListViewId(201L);
        customer.setListViewType(1);
        customer.setEditViewId(202L);

        View view = new View();
        view.setListItems(List.of(customer));

        Object column = new ViewAdapter().getViewInfo(view).getTableColumn().get(0);

        assertEquals(Long.valueOf(201L), columnListViewId(column));
        assertEquals(Integer.valueOf(1), columnListViewType(column));
        assertEquals(Long.valueOf(202L), columnEditViewId(column));
    }

    @Test
    public void viewInfoIncludesLegacyColumnPropertyTypeAndModel() {
        Model customer = new Model();
        customer.setId(700L);
        Property property = new Property();
        property.setName("customer");
        property.setPropertyType(PropertyType.BusinessObject);
        property.setPropertyModel(customer);
        ViewItem customerItem = viewItem("customer", "Customer", ItemEditType.ReadOnly);
        setProperty(customerItem, property);

        View view = new View();
        view.setListItems(List.of(customerItem));

        Object column = new ViewAdapter().getViewInfo(view).getTableColumn().get(0);

        assertEquals(PropertyType.BusinessObject, columnPropertyType(column));
        assertEquals(Long.valueOf(700L), columnPropertyModel(column));
    }

    @Test
    public void viewInfoIncludesLegacyColumnViewFile() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);
        setViewFile(orderId, "order-cell.html");

        View view = new View();
        view.setListItems(List.of(orderId));

        Object column = new ViewAdapter().getViewInfo(view).getTableColumn().get(0);

        assertEquals("order-cell.html", columnViewFile(column));
    }

    @Test
    public void viewInfoIncludesLegacyColumnDefaultPropertyAndEditIds() {
        ViewItem orderId = viewItem("orderId", "Order ID", ItemEditType.ReadOnly);

        View view = new View();
        view.setListItems(List.of(orderId));

        Object column = new ViewAdapter().getViewInfo(view).getTableColumn().get(0);

        assertEquals(Long.valueOf(0L), columnPropertyId(column));
        assertEquals(Long.valueOf(0L), columnEditViewId(column));
        assertEquals(Long.valueOf(0L), columnEditExp(column));
    }

    private static ViewOperation operation(
            String name,
            boolean requireSelect,
            ViewOperationType type,
            View resultView) {
        ViewOperation operation = new ViewOperation();
        operation.setName(name);
        operation.setRequireSelect(requireSelect);
        operation.setType(type);
        operation.setResultView(resultView);
        return operation;
    }

    private static Operation operation(Long id) {
        Operation operation = new Operation();
        operation.setId(id);
        return operation;
    }

    private static ViewItem viewItem(String modelProperty, String label, ItemEditType editType) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setItemLabel(label);
        item.setItemLegend(label);
        item.setInputType(InputType.READ_ONLY);
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

    private static void setWidth(ViewItem item, int width) {
        try {
            ViewItem.class.getMethod("setWidth", Integer.class).invoke(item, width);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy width metadata");
        }
    }

    private static void setProperty(ViewItem item, Property property) {
        try {
            ViewItem.class.getMethod("setProperty", Property.class).invoke(item, property);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy property metadata");
        }
    }

    private static void setViewFile(ViewItem item, String viewFile) {
        try {
            ViewItem.class.getMethod("setViewFile", String.class).invoke(item, viewFile);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy item-file metadata");
        }
    }

    private static Integer columnWidth(Object column) {
        try {
            return (Integer) column.getClass().getMethod("getWidth").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy width metadata");
            return null;
        }
    }

    private static String columnName(Object column) {
        try {
            return (String) column.getClass().getMethod("getName").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy name metadata");
            return null;
        }
    }

    private static Long columnId(Object column) {
        try {
            return (Long) column.getClass().getMethod("getId").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy ID metadata");
            return null;
        }
    }

    private static String columnPropertyName(Object column) {
        try {
            return (String) column.getClass().getMethod("getPropertyName").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy property-name metadata");
            return null;
        }
    }

    private static String columnFormat(Object column) {
        try {
            return (String) column.getClass().getMethod("getFormat").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy format metadata");
            return null;
        }
    }

    private static Boolean columnIsReadOnly(Object column) {
        try {
            return (Boolean) column.getClass().getMethod("getIsReadOnly").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy read-only metadata");
            return null;
        }
    }

    private static ItemEditType columnEditType(Object column) {
        try {
            return (ItemEditType) column.getClass().getMethod("getEditType").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy edit-type metadata");
            return null;
        }
    }

    private static Long columnListViewId(Object column) {
        try {
            return (Long) column.getClass().getMethod("getListViewId").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy list-view ID metadata");
            return null;
        }
    }

    private static Integer columnListViewType(Object column) {
        try {
            return (Integer) column.getClass().getMethod("getListViewType").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy list-view type metadata");
            return null;
        }
    }

    private static PropertyType columnPropertyType(Object column) {
        try {
            return (PropertyType) column.getClass().getMethod("getPropertyType").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy property-type metadata");
            return null;
        }
    }

    private static Long columnPropertyModel(Object column) {
        try {
            return (Long) column.getClass().getMethod("getPropertyModel").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy property-model metadata");
            return null;
        }
    }

    private static String columnViewFile(Object column) {
        try {
            return (String) column.getClass().getMethod("getViewFile").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy view-file metadata");
            return null;
        }
    }

    private static Long columnPropertyId(Object column) {
        try {
            return (Long) column.getClass().getMethod("getPropertyId").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy property ID metadata");
            return null;
        }
    }

    private static Long columnEditViewId(Object column) {
        try {
            return (Long) column.getClass().getMethod("getEditViewId").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy edit-view ID metadata");
            return null;
        }
    }

    private static Long columnEditExp(Object column) {
        try {
            return (Long) column.getClass().getMethod("getEditExp").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy edit-exp metadata");
            return null;
        }
    }

    private static String legacyOperationName(Object operation) {
        try {
            return (String) operation.getClass().getMethod("getName").invoke(operation);
        } catch (ReflectiveOperationException e) {
            fail("OperationInfo should expose legacy name metadata");
            return null;
        }
    }

    private static String legacyName(Object info) {
        try {
            return (String) info.getClass().getMethod("getName").invoke(info);
        } catch (ReflectiveOperationException e) {
            fail("ListViewInfo should expose legacy name metadata");
            return null;
        }
    }

    private static org.fool.framework.view.model.ViewType legacyShowType(Object info) {
        try {
            return (org.fool.framework.view.model.ViewType) info.getClass().getMethod("getShowType").invoke(info);
        } catch (ReflectiveOperationException e) {
            fail("ListViewInfo should expose legacy show type metadata");
            return null;
        }
    }

    private static org.fool.framework.view.model.ViewType legacyType(Object info) {
        try {
            return (org.fool.framework.view.model.ViewType) info.getClass().getMethod("getType").invoke(info);
        } catch (ReflectiveOperationException e) {
            fail("ListViewInfo should expose legacy type metadata");
            return null;
        }
    }

    private static String legacyTempFile(Object info) {
        try {
            return (String) info.getClass().getMethod("getTempFile").invoke(info);
        } catch (ReflectiveOperationException e) {
            fail("ListViewInfo should expose legacy temp file metadata");
            return null;
        }
    }

    private static Long detailViewId(Object info) {
        try {
            return (Long) info.getClass().getMethod("getDetailViewId").invoke(info);
        } catch (ReflectiveOperationException e) {
            fail("ListViewInfo should expose legacy detail view ID metadata");
            return null;
        }
    }
}
