package org.fool.framework.view.service;

import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.ItemEditType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.model.ViewType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LegacyAutoViewFactoryTest {
    @Test
    public void createsLegacyDefaultListAndDetailViewsFromModelProperties() {
        Model order = model("Order", ModelType.DYNAMIC);
        Property id = property("id", "ID", false);
        Property name = property("name", "名称", false);
        Property lines = property("lines", "明细", true);
        order.setProperties(List.of(id, name, lines));
        Operation delete = new Operation();
        delete.setName("删除");
        delete.setBaseOperationType(OperationBaseType.DELETE);
        order.setOperations(List.of(delete));

        LegacyAutoViewFactory factory = new LegacyAutoViewFactory();

        View listView = factory.createDefaultListView(order);
        View detailView = factory.createDefaultItemView(order);

        assertEquals("Order列表", listView.getViewName());
        assertEquals("Order列表", listView.getViewTitle());
        assertEquals(ViewType.ListView, listView.getViewType());
        assertEquals("", listView.getFilter());
        assertSame(detailView, listView.getDefaultDetailView());
        assertEquals(List.of("id", "name"), listView.getListItems().stream().map(item -> item.getModelProperty()).toList());
        assertTrue(listView.getListItems().stream().allMatch(item -> item.getInputType() == InputType.READ_ONLY));
        assertTrue(listView.getListItems().stream().allMatch(item -> item.getEditType() == ItemEditType.ReadOnly));
        assertTrue(listView.getListItems().stream().noneMatch(ViewItemAssertions::canEdit));

        assertEquals("Order详细", detailView.getViewName());
        assertEquals("Order详细", detailView.getViewTitle());
        assertEquals(ViewType.DetailView, detailView.getViewType());
        assertEquals(List.of("id", "name", "lines"), detailView.getListItems().stream().map(item -> item.getModelProperty()).toList());
        assertTrue(detailView.getListItems().stream().allMatch(ViewItemAssertions::canEdit));
        assertFalse(detailView.getListItems().stream().anyMatch(item -> item.getInputType() == InputType.READ_ONLY));
        assertTrue(detailView.getListItems().stream().allMatch(item -> item.getEditType() == ItemEditType.TextBox));

        assertEquals(List.of("新建", "编辑", "删除"), listView.getOperations().stream().map(ViewOperation::getName).toList());
        assertEquals(List.of(false, true, true), listView.getOperations().stream().map(ViewOperation::isRequireSelect).toList());
        assertSame(delete, listView.getOperations().get(2).getOperation());
        assertEquals("确定要删除？该操作不可撤消", listView.getOperations().get(2).getConfirmMsg());
        assertEquals("操作成功", listView.getOperations().get(2).getSuccessMsg());
    }

    private static Model model(String name, ModelType type) {
        Model model = new Model();
        model.setName(name);
        model.setText(name);
        model.setClassName("example." + name);
        model.setModelType(type);
        return model;
    }

    private static Property property(String name, String text, boolean collection) {
        Property property = new Property();
        property.setName(name);
        property.setRemark(text);
        property.setColumn(name.toUpperCase());
        property.setIsCollection(collection);
        return property;
    }

    private static final class ViewItemAssertions {
        private static boolean canEdit(org.fool.framework.view.model.ViewItem item) {
            return item.isCanEdit();
        }
    }
}
