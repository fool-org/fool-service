package org.fool.framework.view.api;

import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.InputQueryRequest;
import org.fool.framework.view.dto.InputQueryResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataControllerInputQueryTest {
    @Test
    public void inputQueryPassesLegacyRequestToService() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        InputQueryResult expected = new InputQueryResult();
        expected.setItems(List.of(new InputQueryResult.QueryItem("1001", "Ada")));
        InputQueryRequest request = new InputQueryRequest();
        request.setViewName("OrderList");
        request.setViewItemId("customer");
        request.setText("Ad");
        when(dataQueryService.inputQuery(request)).thenReturn(expected);

        DataController controller = new DataController();
        setField(controller, "dataQueryService", dataQueryService);

        CommonResponse<InputQueryResult> response = controller.inputQuery(request);

        verify(dataQueryService).inputQuery(request);
        assertEquals(0, response.getCode());
        assertSame(expected, response.getData());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
