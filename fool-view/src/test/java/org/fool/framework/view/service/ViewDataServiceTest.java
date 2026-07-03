package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.PersistedViewOperation;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperationType;
import org.mockito.ArgumentCaptor;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
        row.setErrorMsg("操作失败");
        OperationCommand command = new OperationCommand();
        command.setCommandType(CommandsType.SET_VALUE);
        command.setPropertyId(1003L);
        command.setExpression("$1");
        command.setIndex(1);

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(PersistedViewOperation.class), anyString(), eq(100L))).thenReturn(List.of(row));
        when(daoService.selectList(eq(OperationCommand.class), anyString(), eq(7001L))).thenReturn(List.of(command));

        View result = service.getViewData("100", "");

        assertEquals(1, result.getOperations().size());
        assertEquals(7001L, result.getOperations().get(0).getOperation().getId().longValue());
        assertEquals(OperationBaseType.DELETE, result.getOperations().get(0).getOperation().getBaseOperationType());
        assertEquals(ViewOperationType.COMMAND, result.getOperations().get(0).getType());
        assertEquals("操作成功", result.getOperations().get(0).getSuccessMsg());
        assertEquals("操作失败", result.getOperations().get(0).getErrorMsg());
        assertEquals(1, result.getOperations().get(0).getOperation().getCommands().size());
        assertEquals(CommandsType.SET_VALUE,
                result.getOperations().get(0).getOperation().getCommands().get(0).getCommandType());
        assertEquals(1003L,
                result.getOperations().get(0).getOperation().getCommands().get(0).getPropertyId().longValue());
    }

    @Test
    public void getViewDataHydratesLegacyOperationCommandArgColumns() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        PersistedViewOperation row = new PersistedViewOperation();
        row.setOperationId(7001L);
        row.setOperationBaseType(OperationBaseType.UPDATE);

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(PersistedViewOperation.class), anyString(), eq(100L))).thenReturn(List.of(row));
        when(daoService.selectList(eq(OperationCommand.class), anyString(), eq(7001L))).thenReturn(List.of());

        service.getViewData("100", "");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(daoService).selectList(eq(OperationCommand.class), sql.capture(), eq(7001L));
        assertTrue(hasField(OperationCommand.class, "argModelId"));
        assertTrue(hasField(OperationCommand.class, "argExpression"));
        assertTrue(hasField(OperationCommand.class, "argSourceIdExpression"));
        assertTrue(hasField(OperationCommand.class, "propertyExpression"));
        assertTrue(hasField(OperationCommand.class, "tempValue"));
        assertTrue(sql.getValue().contains("`SW_SYS_COMMAND_ARGMODEL`"));
        assertTrue(sql.getValue().contains("`SW_SYS_COMMAND_ARGEXP`"));
        assertTrue(sql.getValue().contains("`SW_SYS_COMMAND_ARGID`"));
        assertTrue(sql.getValue().contains("`SW_SYS_COMMAND_PROPERTY_EXP`"));
        assertTrue(sql.getValue().contains("`SW_SYS_COMMAND_TEMPVALUE`"));
    }

    private static boolean hasField(Class<?> type, String name) {
        return Arrays.stream(type.getDeclaredFields()).anyMatch(field -> name.equals(field.getName()));
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
