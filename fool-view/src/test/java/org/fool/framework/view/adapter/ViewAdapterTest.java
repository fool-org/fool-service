package org.fool.framework.view.adapter;

import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
