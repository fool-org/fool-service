package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewOperation;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private static Model model() {
        Model model = new Model();
        model.setName("Order");
        model.setTableName("market_order");
        Property orderId = new Property();
        orderId.setName("orderId");
        orderId.setColumn("order_id");
        orderId.setPropertyType(PropertyType.Long);
        model.setIdProperty(orderId);
        model.setProperties(List.of(orderId));
        return model;
    }
}
