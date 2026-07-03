package org.fool.framework.view.api;

import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertSame;
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

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
