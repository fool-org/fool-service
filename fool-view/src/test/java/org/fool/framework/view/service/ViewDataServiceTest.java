package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dto.CommonException;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.OperationViewParam;
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
import static org.junit.Assert.assertThrows;
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
    public void getViewDataRejectsBusinessNameShortcut() {
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", mock(DaoService.class));

        CommonException exception = assertThrows(CommonException.class, () -> service.getViewData("OrderList", ""));

        assertEquals("ViewId is required", exception.getMessage());
    }

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

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);

        View result = service.getViewData("100", "");

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
    public void getViewDataHydratesLegacyDefaultDetailView() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        ViewDataService.DefaultDetailViewRow row = new ViewDataService.DefaultDetailViewRow();
        row.viewId = 102L;

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(ViewDataService.DefaultDetailViewRow.class), anyString(), eq(100L)))
                .thenReturn(List.of(row));

        View result = service.getViewData("100", "");

        assertEquals(Long.valueOf(102L), result.getDefaultDetailView().getId());
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(daoService).selectList(eq(ViewDataService.DefaultDetailViewRow.class), sql.capture(), eq(100L));
        assertTrue(sql.getValue().contains("`VIEW_DEFAULT`"));
        assertTrue(sql.getValue().contains("`SW_SYS_VIEW`"));
    }

    @Test
    public void getViewDataHydratesLegacyViewAndItemTemplateFiles() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        ViewItem item = new ViewItem();
        item.setId(901L);
        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        view.setListItems(List.of(item));
        ViewDataService.ViewTempFileRow viewFile = new ViewDataService.ViewTempFileRow();
        viewFile.fileName = "viewWithChart";
        ViewDataService.ViewItemFileRow itemFile = new ViewDataService.ViewItemFileRow();
        itemFile.itemId = 901L;
        itemFile.fileName = "./includes/List";

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(ViewDataService.ViewTempFileRow.class), anyString(), eq(100L)))
                .thenReturn(List.of(viewFile));
        when(daoService.selectList(eq(ViewDataService.ViewItemFileRow.class), anyString(), eq(100L)))
                .thenReturn(List.of(itemFile));

        View result = service.getViewData("100", "");

        assertEquals("viewWithChart", result.getTempFile());
        assertEquals("./includes/List", result.getListItems().get(0).getViewFile());
    }

    @Test
    public void getViewDataHydratesLinkedListViewMetadataAndItems() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        ViewItem item = new ViewItem();
        item.setId(901L);
        item.setListViewId(201L);
        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        view.setListItems(List.of(item));
        ViewItem childItem = new ViewItem();
        childItem.setModelProperty("itemName");
        View listView = new View();
        listView.setViewName("OrderItemList");
        listView.setViewModel("OrderItem");
        listView.setViewType(org.fool.framework.view.model.ViewType.DetailView);
        listView.setListItems(List.of(childItem));
        Property itemName = new Property();
        itemName.setName("itemName");
        Model itemModel = new Model();
        itemModel.setProperties(List.of(itemName));

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(View.class, "201")).thenReturn(listView);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.getOneDetailByKey(Model.class, "OrderItem")).thenReturn(itemModel);

        View result = service.getViewData("100", "");

        assertEquals(Integer.valueOf(1), result.getListItems().get(0).getListViewType());
        assertEquals("OrderItemList", result.getListItems().get(0).getListViewName());
        assertSame(listView, result.getListItems().get(0).getListView());
        assertSame(itemName, childItem.getProperty());
    }

    @Test
    public void getViewDataHydratesLegacyOperationViewParams() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        PersistedViewOperation row = new PersistedViewOperation();
        row.setName("保存");
        row.setOperationId(7002L);
        row.setOperationViewId(8002L);

        OperationViewParam param = new OperationViewParam();
        param.setId(8101L);
        param.setName("审批意见");
        param.setIndex(1);
        param.setParamId(7201L);
        param.setParamName("remark");
        param.setViewId(100L);
        param.setFilter("state=0");
        param.setValue("ok");

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(PersistedViewOperation.class), anyString(), eq(100L))).thenReturn(List.of(row));
        when(daoService.selectList(eq(OperationViewParam.class), anyString(), eq(8002L))).thenReturn(List.of(param));
        when(daoService.selectList(eq(OperationCommand.class), anyString(), eq(7002L))).thenReturn(List.of());

        View result = service.getViewData("100", "");

        OperationViewParam hydrated = result.getOperations().get(0).getParams().get(0);
        assertEquals(Long.valueOf(8101L), hydrated.getId());
        assertEquals("审批意见", hydrated.getName());
        assertEquals(Integer.valueOf(1), hydrated.getIndex());
        assertEquals(Long.valueOf(7201L), hydrated.getParamId());
        assertEquals("remark", hydrated.getParamName());
        assertEquals(Long.valueOf(100L), hydrated.getViewId());
        assertEquals("state=0", hydrated.getFilter());
        assertEquals("ok", hydrated.getValue());

        ArgumentCaptor<String> operationSql = ArgumentCaptor.forClass(String.class);
        verify(daoService).selectList(eq(PersistedViewOperation.class), operationSql.capture(), eq(100L));
        assertTrue(operationSql.getValue().contains("opv.`SW_SYS_OPVIEW_OPREATION` AS `SW_VIEW_OPERATION_MODELOPERATION`"));
        assertTrue(operationSql.getValue().contains("opv.`SysId` = vo.`SW_VIEW_OPERATION_MODELOPERATION`"));
        assertTrue(operationSql.getValue().contains("opv.`SysId` AS `operation_view_id`"));

        ArgumentCaptor<String> paramSql = ArgumentCaptor.forClass(String.class);
        verify(daoService).selectList(eq(OperationViewParam.class), paramSql.capture(), eq(8002L));
        assertTrue(paramSql.getValue().contains("`SW_SYS_OPERATIONVIEW_ITEM`"));
        assertTrue(paramSql.getValue().contains("`SW_SYS_OPERATION_PARAM`"));
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

    @Test
    public void getViewDataHydratesLegacyOperationInvokeColumns() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        PersistedViewOperation row = new PersistedViewOperation();
        row.setName("反射");
        row.setOperationId(7003L);
        row.setOperationBaseType(OperationBaseType.ASSEBMLY);
        row.setFilter("order_state='0'");
        row.setArgModelId(200L);
        row.setArgFilter("arg_id=.orderId");
        row.setInvokeDll("Legacy.dll");
        row.setInvokeClass("Legacy.Worker");
        row.setInvokeMethod("Run");
        row.setReturnModelId(300L);

        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(new Model());
        when(daoService.selectList(eq(PersistedViewOperation.class), anyString(), eq(100L))).thenReturn(List.of(row));
        when(daoService.selectList(eq(OperationCommand.class), anyString(), eq(7003L))).thenReturn(List.of());

        View result = service.getViewData("100", "");

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(daoService).selectList(eq(PersistedViewOperation.class), sql.capture(), eq(100L));
        Operation operation = result.getOperations().get(0).getOperation();
        assertEquals("order_state='0'", operation.getFilter());
        assertEquals(200L, operation.getArgModelId().longValue());
        assertEquals("arg_id=.orderId", operation.getArgFilter());
        assertEquals("Legacy.dll", operation.getInvokeDll());
        assertEquals("Legacy.Worker", operation.getInvokeClass());
        assertEquals("Run", operation.getInvokeMethod());
        assertEquals(300L, operation.getReturnModelId().longValue());
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_FILTER`"));
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_ARGMODEL`"));
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_ARGFILTER`"));
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_INVOKEDLL`"));
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_INVOKECLASS`"));
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_INVOKEMETHOD`"));
        assertTrue(sql.getValue().contains("op.`SW_MODEL_OPERATION_RETURNMODEL`"));
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
