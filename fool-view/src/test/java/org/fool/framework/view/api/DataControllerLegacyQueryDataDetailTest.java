package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.LegacyQueryDataDetailRequest;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataControllerLegacyQueryDataDetailTest {
    @Test
    public void queryDataDetailMapsLegacyViewIdAndObjectId() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(dataQueryService.queryLegacyViewDataDetail("100", "1001", "$1001")).thenReturn(expected);

        DataController controller = new DataController();
        setField(controller, "dataQueryService", dataQueryService);
        LegacyQueryDataDetailRequest request = new LegacyQueryDataDetailRequest();
        request.setViewId(100L);
        request.setObjId("1001");
        request.setIdExp("$1001");

        CommonResponse<QueryDataDetailResult> response = controller.queryDataDetail(request);

        verify(dataQueryService).queryLegacyViewDataDetail("100", "1001", "$1001");
        assertEquals(0, response.getCode());
        assertSame(expected, response.getData());
    }

    @Test
    public void queryDataDetailAcceptsLegacyPascalAliases() throws Exception {
        LegacyQueryDataDetailRequest request = new ObjectMapper().readValue(
                "{\"ViewId\":100,\"ObjId\":\"1001\",\"IdExp\":\"#.id\"}",
                LegacyQueryDataDetailRequest.class);

        assertEquals(Long.valueOf(100L), request.getViewId());
        assertEquals("1001", request.getObjId());
        assertEquals("#.id", request.getIdExp());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
