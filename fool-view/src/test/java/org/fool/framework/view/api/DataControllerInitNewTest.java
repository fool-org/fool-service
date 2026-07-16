package org.fool.framework.view.api;

import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.LegacyInitNewRequest;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataControllerInitNewTest {
    @Test
    public void initNewMapsLegacyViewIdAndParentObjectId() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(dataQueryService.initLegacyNewObject("100", "5001")).thenReturn(expected);

        DataController controller = new DataController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        LegacyInitNewRequest request = new LegacyInitNewRequest();
        request.setViewId(100L);
        request.setParentObjId("5001");

        CommonResponse<QueryDataDetailResult> response = controller.initNew(request);

        verify(dataQueryService).initLegacyNewObject("100", "5001");
        assertEquals(0, response.getCode());
        assertSame(expected, response.getData());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
