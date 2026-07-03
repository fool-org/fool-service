package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewOperation;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class DataQueryServiceRunOperationTest {
    @Test
    public void runLegacyDeleteOperationDeletesObjectAndReturnsSuccessMessage() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(deleteOperation(7001L, "操作成功"));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.deleteData(data)).thenReturn(true);
        LegacyRunOperationRequest request = request("1001", 100L, 7001L);

        LegacyRunOperationResult result = service.runLegacyOperation(request);

        verify(modelDataService).deleteData(data);
        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getReturnMsg());
        assertNull(result.getReturnObjId());
        assertEquals(0L, result.getReturnViewId());
    }

    @Test
    public void runLegacyUpdateOperationSavesObjectAndReturnsSuccessMessage() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operation(7002L, OperationBaseType.UPDATE, "保存成功"));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals("保存成功", result.getReturnMsg());
    }

    @Test
    public void runLegacyCreateOperationCreatesObjectAndReturnsSuccessMessage() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operation(7004L, OperationBaseType.CREATE, "创建成功"));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "970733");
        data.set("symbol", "SOL-USDT");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "970733")).thenReturn(data);
        when(modelDataService.createData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("970733", 100L, 7004L));

        verify(modelDataService).createData(data);
        assertTrue(result.isSuccess());
        assertEquals("创建成功", result.getReturnMsg());
    }

    @Test
    public void runLegacyUpdateOperationAppliesSetValueCommandsBeforeSave() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1003L, "$1", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        data.set("state", "0");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals("1", data.get("state"));
    }

    @Test
    public void runLegacyUpdateOperationConvertsStaticSetValueToTargetPropertyType() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1004L, "$7", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals(Integer.valueOf(7), data.get("retryCount"));
    }

    @Test
    public void runLegacyUpdateOperationEvaluatesSetValueMathExpression() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1004L, ".retryCount+$2", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        data.set("retryCount", 7);
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals(Integer.valueOf(9), data.get("retryCount"));
    }

    @Test
    public void runLegacyUpdateOperationConvertsStaticSetValueScalarTypes() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1005L, "$true", 1),
                command(CommandsType.SET_VALUE, 1006L, "$2", 2),
                command(CommandsType.SET_VALUE, 1007L, "$A", 3),
                command(CommandsType.SET_VALUE, 1008L, "$123", 4),
                command(CommandsType.SET_VALUE, 1009L, "$12.50", 5),
                command(CommandsType.SET_VALUE, 1010L, "$1.5", 6),
                command(CommandsType.SET_VALUE, 1011L, "$2026-07-03T10:00:00", 7)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals(Boolean.TRUE, data.get("confirmed"));
        assertEquals(Byte.valueOf((byte) 2), data.get("byteCode"));
        assertEquals(Character.valueOf('A'), data.get("marker"));
        assertEquals(Integer.valueOf(123), data.get("longCode"));
        assertEquals(new BigDecimal("12.50"), data.get("amount"));
        assertEquals(Double.valueOf(1.5D), data.get("ratio"));
        assertEquals(LocalDateTime.of(2026, 7, 3, 10, 0), data.get("startsAt"));
    }

    @Test
    public void runLegacyUpdateOperationLoadsStaticBusinessObjectSetValue() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        Model customerModel = new Model();
        customerModel.setName("Customer");
        model.getProperties().stream()
                .filter(property -> property.getId().equals(1012L))
                .findFirst()
                .orElseThrow()
                .setPropertyModel(customerModel);
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1012L, "$C001", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        DbMysqlDynamic customer = new DbMysqlDynamic(customerModel);
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.getOneData("Customer", "C001")).thenReturn(customer);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals(customer, data.get("customer"));
    }

    @Test
    public void runLegacyUpdateOperationAppliesSetValueFromDateTimeContext() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1011L, "@datetime", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertTrue(data.get("startsAt") instanceof LocalDateTime);
    }

    @Test
    public void runLegacyUpdateOperationAppliesSetValueFromCurrentObjectProperty() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.SET_VALUE, 1003L, ".symbol", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        data.set("symbol", "BTC-USDT");
        data.set("state", "0");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
        assertEquals("BTC-USDT", data.get("state"));
    }

    @Test
    public void runLegacyUpdateOperationStopsWhenFilterCommandRejectsObject() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        OperationCommand filter = command(CommandsType.FILTER, null, "`order_state`='1'", 1);
        filter.setPropertyExpression("状态不允许");
        ViewOperation operation = operation(7002L, OperationBaseType.UPDATE, "保存成功", "保存失败");
        operation.getOperation().setCommands(List.of(filter));
        View view = view(operation);
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        data.set("state", "0");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.getDataList(eq("Order"), any(IQueryFilter.class), eq(model.getProperties())))
                .thenReturn(List.of());

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(modelDataService, never()).saveData(data);
        assertFalse(result.isSuccess());
        assertTrue(result.getReturnMsg().startsWith("保存失败"));
        assertTrue(result.getReturnMsg().contains("状态不允许"));
    }

    @Test
    public void runLegacyUpdateOperationInvokesPropertyModelMethodCommand() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.EXUTE_PROPRTY_MODEL_METHOD, 1012L, "Deactivate", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        IDynamicData customer = mock(IDynamicData.class);
        data.set("orderId", "1001");
        data.set("customer", customer);
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(customer).invoke("Deactivate");
        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
    }

    @Test
    public void runLegacyUpdateOperationInvokesPropertyModelMethodForEachCollectionItem() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operationWithCommand(7002L, OperationBaseType.UPDATE, "保存成功",
                command(CommandsType.EXUTE_PROPRTY_MODEL_METHOD, 1013L, "Close", 1)));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        IDynamicData firstItem = mock(IDynamicData.class);
        IDynamicData secondItem = mock(IDynamicData.class);
        data.set("orderId", "1001");
        data.set("items", List.of(firstItem, secondItem));
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenReturn(true);

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        verify(firstItem).invoke("Close");
        verify(secondItem).invoke("Close");
        verify(modelDataService).saveData(data);
        assertTrue(result.isSuccess());
    }

    @Test
    public void runLegacyOperationReturnsLegacyErrorMessageWhenExecutionFails() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        Model model = model();
        View view = view(operation(7002L, OperationBaseType.UPDATE, "保存成功", "保存失败"));
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("orderId", "1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(modelDataService.saveData(data)).thenThrow(new IllegalStateException("db down"));

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7002L));

        assertFalse(result.isSuccess());
        assertTrue(result.getReturnMsg().startsWith("保存失败"));
        assertTrue(result.getReturnMsg().contains("db down"));
    }

    @Test
    public void runLegacyOperationLeavesUnsupportedOperationUnexecuted() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        DataQueryService service = service(daoService, modelDataService, viewDataService);
        View view = view(operation(7003L, OperationBaseType.JSONPOST, "posted"));
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model());

        LegacyRunOperationResult result = service.runLegacyOperation(request("1001", 100L, 7003L));

        verify(modelDataService, never()).deleteData(org.mockito.ArgumentMatchers.any(IDynamicData.class));
        assertFalse(result.isSuccess());
        assertEquals("", result.getReturnMsg());
    }

    private static DataQueryService service(
            DaoService daoService,
            ModelDataService modelDataService,
            ViewDataService viewDataService) {
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        return service;
    }

    private static LegacyRunOperationRequest request(String objectId, Long viewId, Long operationId) {
        LegacyRunOperationRequest request = new LegacyRunOperationRequest();
        request.setObjectId(objectId);
        request.setViewId(viewId);
        request.setOperationId(operationId);
        return request;
    }

    private static View view(ViewOperation operation) {
        View view = new View();
        view.setId(100L);
        view.setViewModel("Order");
        view.setOperations(List.of(operation));
        return view;
    }

    private static ViewOperation deleteOperation(Long id, String successMsg) {
        return operation(id, OperationBaseType.DELETE, successMsg);
    }

    private static ViewOperation operation(Long id, OperationBaseType type, String successMsg) {
        return operation(id, type, successMsg, null);
    }

    private static ViewOperation operation(Long id, OperationBaseType type, String successMsg, String errorMsg) {
        Operation operation = new Operation();
        operation.setId(id);
        operation.setBaseOperationType(type);
        ViewOperation viewOperation = new ViewOperation();
        viewOperation.setOperation(operation);
        viewOperation.setSuccessMsg(successMsg);
        viewOperation.setErrorMsg(errorMsg);
        return viewOperation;
    }

    private static ViewOperation operationWithCommand(
            Long id,
            OperationBaseType type,
            String successMsg,
            OperationCommand... commands) {
        ViewOperation viewOperation = operation(id, type, successMsg);
        viewOperation.getOperation().setCommands(List.of(commands));
        return viewOperation;
    }

    private static OperationCommand command(CommandsType type, Long propertyId, String expression, int index) {
        OperationCommand command = new OperationCommand();
        command.setCommandType(type);
        command.setPropertyId(propertyId);
        command.setExpression(expression);
        command.setIndex(index);
        return command;
    }

    private static Model model() {
        Model model = new Model();
        model.setName("Order");
        model.setTableName("market_order");
        Property orderId = new Property();
        orderId.setId(1001L);
        orderId.setName("orderId");
        orderId.setColumn("order_id");
        orderId.setPropertyType(PropertyType.Long);
        Property state = new Property();
        state.setId(1003L);
        state.setName("state");
        state.setColumn("order_state");
        state.setPropertyType(PropertyType.String);
        Property symbol = new Property();
        symbol.setId(1002L);
        symbol.setName("symbol");
        symbol.setColumn("order_symbol");
        symbol.setPropertyType(PropertyType.String);
        Property retryCount = new Property();
        retryCount.setId(1004L);
        retryCount.setName("retryCount");
        retryCount.setColumn("retry_count");
        retryCount.setPropertyType(PropertyType.Int);
        Property confirmed = new Property();
        confirmed.setId(1005L);
        confirmed.setName("confirmed");
        confirmed.setColumn("confirmed");
        confirmed.setPropertyType(PropertyType.Boolean);
        Property byteCode = new Property();
        byteCode.setId(1006L);
        byteCode.setName("byteCode");
        byteCode.setColumn("byte_code");
        byteCode.setPropertyType(PropertyType.Byte);
        Property marker = new Property();
        marker.setId(1007L);
        marker.setName("marker");
        marker.setColumn("marker");
        marker.setPropertyType(PropertyType.Char);
        Property longCode = new Property();
        longCode.setId(1008L);
        longCode.setName("longCode");
        longCode.setColumn("long_code");
        longCode.setPropertyType(PropertyType.Long);
        Property amount = new Property();
        amount.setId(1009L);
        amount.setName("amount");
        amount.setColumn("amount");
        amount.setPropertyType(PropertyType.Decimal);
        Property ratio = new Property();
        ratio.setId(1010L);
        ratio.setName("ratio");
        ratio.setColumn("ratio");
        ratio.setPropertyType(PropertyType.Double);
        Property startsAt = new Property();
        startsAt.setId(1011L);
        startsAt.setName("startsAt");
        startsAt.setColumn("starts_at");
        startsAt.setPropertyType(PropertyType.DateTime);
        Property customer = new Property();
        customer.setId(1012L);
        customer.setName("customer");
        customer.setColumn("customer_id");
        customer.setPropertyType(PropertyType.BusinessObject);
        Property items = new Property();
        items.setId(1013L);
        items.setName("items");
        items.setColumn("order_items_list");
        items.setPropertyType(PropertyType.BusinessObject);
        items.setIsCollection(true);
        model.setIdProperty(orderId);
        model.setProperties(List.of(orderId, symbol, state, retryCount, confirmed,
                byteCode, marker, longCode, amount, ratio, startsAt, customer, items));
        return model;
    }
}
