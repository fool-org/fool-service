package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataControllerRunOperationTest {
    @Test
    public void runOperationPassesLegacyRequestToService() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DataController controller = new DataController();
        setField(controller, "dataQueryService", dataQueryService);
        LegacyRunOperationRequest request = new LegacyRunOperationRequest();
        LegacyRunOperationResult expected = new LegacyRunOperationResult();
        when(dataQueryService.runLegacyOperation(request)).thenReturn(expected);

        CommonResponse<LegacyRunOperationResult> response = controller.runOperation(request);

        verify(dataQueryService).runLegacyOperation(request);
        assertSame(expected, response.getData());
    }

    @Test
    public void runOperationResultExposesLegacyPascalAliases() {
        LegacyRunOperationResult result = new LegacyRunOperationResult();
        result.setSuccess(true);
        result.setReturnMsg("执行成功");
        result.setReturnObjId("1001");
        result.setReturnViewId(200L);
        result.setValue(List.of(new ListDataValue()));

        Map<?, ?> serialized = new ObjectMapper().convertValue(result, Map.class);
        assertEquals(Boolean.TRUE, serialized.get("IsSuccess"));
        assertEquals("执行成功", serialized.get("ReturnMsg"));
        assertEquals("1001", serialized.get("ReturnObjId"));
        assertEquals(200L, serialized.get("ReturnViewId"));
        assertTrue(serialized.containsKey("Value"));
    }

    @Test
    public void runOperationAcceptsLegacyWebPayloadAliases() throws Exception {
        LegacyRunOperationRequest request = new ObjectMapper().readValue(
                "{\"objid\":\"1001\",\"viewid\":100,\"opid\":7002}",
                LegacyRunOperationRequest.class);

        assertEquals("1001", request.getObjectId());
        assertEquals(Long.valueOf(100L), request.getViewId());
        assertEquals(Long.valueOf(7002L), request.getOperationId());
    }

    @Test
    public void runOperationKeepsLegacyExoperationRouteAlias() throws Exception {
        PostMapping mapping = DataController.class
                .getMethod("runOperation", LegacyRunOperationRequest.class)
                .getAnnotation(PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/runoperation"));
        assertTrue(List.of(mapping.value()).contains("/exoperation"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
