package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.PersistedViewOperation;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperationType;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ViewDataServiceTest {
    @Test
    public void getViewDataAttachesLegacyModelPropertyMetadataToItems() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        ViewItem item = new ViewItem();
        item.setModelProperty("customer");
        View view = new View();
        view.setViewModel("Order");
        view.setListItems(List.of(item));
        Property property = new Property();
        property.setName("customer");
        property.setPropertyType(PropertyType.BusinessObject);
        Model model = new Model();
        model.setName("Order");
        model.setProperties(List.of(property));

        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);

        View result = service.getViewData("OrderList", "");

        assertSame(property, itemProperty(result.getListItems().get(0)));
    }

    @Test
    public void getViewDataAttachesPersistedOperations() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        PersistedViewOperation row = new PersistedViewOperation();
        row.setName("删除");
        row.setOperationId(7001L);
        row.setOperationBaseType(OperationBaseType.DELETE);
        row.setLocation(2);
        row.setRequireSelect(true);
        row.setSuccessMsg("操作成功");

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(PersistedViewOperation.class), anyString(), eq(100L))).thenReturn(List.of(row));

        View result = service.getViewData("100", "");

        assertEquals(1, result.getOperations().size());
        assertEquals(7001L, result.getOperations().get(0).getOperation().getId().longValue());
        assertEquals(OperationBaseType.DELETE, result.getOperations().get(0).getOperation().getBaseOperationType());
        assertEquals(ViewOperationType.COMMAND, result.getOperations().get(0).getType());
        assertEquals("操作成功", result.getOperations().get(0).getSuccessMsg());
    }

    private static Property itemProperty(ViewItem item) {
        try {
            return (Property) item.getClass().getMethod("getProperty").invoke(item);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy property metadata");
            return null;
        }
    }
}
