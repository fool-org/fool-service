package org.fool.framework.view.adapter;

import org.fool.framework.model.model.Operation;
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

    private static Integer columnWidth(Object column) {
        try {
            return (Integer) column.getClass().getMethod("getWidth").invoke(column);
        } catch (ReflectiveOperationException e) {
            fail("TableColumnInfo should expose legacy width metadata");
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

    private static Long detailViewId(Object info) {
        try {
            return (Long) info.getClass().getMethod("getDetailViewId").invoke(info);
        } catch (ReflectiveOperationException e) {
            fail("ListViewInfo should expose legacy detail view ID metadata");
            return null;
        }
    }
}
