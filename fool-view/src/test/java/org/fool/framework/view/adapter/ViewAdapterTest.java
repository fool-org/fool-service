package org.fool.framework.view.adapter;

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
        ViewOperation delete = operation("Delete", true, ViewOperationType.COMMAND, null);

        View view = new View();
        view.setListItems(List.of());
        view.setOperations(List.of(create, delete));

        ListViewInfo info = new ViewAdapter().getViewInfo(view);

        assertEquals(2, info.getOperations().size());
        assertEquals("Create", info.getOperations().get(0).getText());
        assertFalse(info.getOperations().get(0).isRequireSelect());
        assertEquals(ViewOperationType.MODAL_DETAIL_VIEW, info.getOperations().get(0).getType());
        assertEquals("OrderDetail", info.getOperations().get(0).getViewName());
        assertEquals(Long.valueOf(200L), info.getOperations().get(0).getViewId());

        assertEquals("Delete", info.getOperations().get(1).getText());
        assertTrue(info.getOperations().get(1).isRequireSelect());
        assertEquals(ViewOperationType.COMMAND, info.getOperations().get(1).getType());
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

    private static ViewItem viewItem(String modelProperty, String label, ItemEditType editType) {
        ViewItem item = new ViewItem();
        item.setModelProperty(modelProperty);
        item.setItemLabel(label);
        item.setItemLegend(label);
        item.setInputType(InputType.READ_ONLY);
        item.setEditType(editType);
        return item;
    }
}
