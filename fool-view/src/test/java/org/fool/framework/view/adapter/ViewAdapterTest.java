package org.fool.framework.view.adapter;

import org.fool.framework.common.PropertyType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.model.ViewOperationType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

public class ViewAdapterTest {
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
    public void viewOperationsAreMappedToLegacyOperationInfo() {
        View detailView = new View();
        detailView.setId(200L);
        detailView.setViewName("OrderDetail");

        ViewOperation create = operation("Create", false, ViewOperationType.MODAL_DETAIL_VIEW, detailView);
        create.setLocation(0);
        create.setOperation(operation(300L));
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
