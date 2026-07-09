package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.InputQueryRequest;
import org.fool.framework.view.dto.InputQueryResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataControllerInputQueryTest {
    @Test
    public void inputQueryAcceptsLegacyPascalRequestAliases() throws Exception {
        InputQueryRequest request = new ObjectMapper().readValue(
                "{\"Text\":\"Ad\",\"ViewId\":100,\"ViewName\":\"OrderList\",\"ViewItemId\":\"customer\",\"ModelID\":\"103\",\"ObjID\":\"1001\",\"OwnerId\":\"5001\",\"IsAdded\":true}",
                InputQueryRequest.class);

        assertEquals("Ad", request.getText());
        assertEquals(Long.valueOf(100), request.getViewId());
        assertEquals("OrderList", request.getViewName());
        assertEquals("customer", request.getViewItemId());
        assertEquals("103", request.getModelID());
        assertEquals("1001", request.getObjID());
        assertEquals("5001", request.getOwnerId());
        assertEquals(true, request.isAdded());
    }

    @Test
    public void inputQueryAcceptsLegacyWebRequestAliases() throws Exception {
        InputQueryRequest request = new ObjectMapper().readValue(
                "{\"viewid\":100,\"itemid\":\"customer\",\"text\":\"Ad\",\"objid\":\"1001\",\"ownerid\":\"5001\",\"newadd\":true}",
                InputQueryRequest.class);

        assertEquals("Ad", request.getText());
        assertEquals(Long.valueOf(100), request.getViewId());
        assertEquals("customer", request.getViewItemId());
        assertEquals("1001", request.getObjID());
        assertEquals("5001", request.getOwnerId());
        assertEquals(true, request.isAdded());
    }

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

    @Test
    public void inputQueryResultExposesLegacyPascalAliases() {
        InputQueryResult result = new InputQueryResult();
        result.setItems(List.of(new InputQueryResult.QueryItem("1001", "Ada")));

        Map<?, ?> serialized = new ObjectMapper().convertValue(result, Map.class);
        assertTrue(serialized.containsKey("items"));
        assertTrue(serialized.containsKey("Items"));
        assertEquals(serialized.get("items"), serialized.get("Items"));
        Map<?, ?> item = (Map<?, ?>) ((List<?>) serialized.get("Items")).get(0);
        assertEquals("1001", item.get("id"));
        assertEquals("1001", item.get("Id"));
        assertEquals("Ada", item.get("text"));
        assertEquals("Ada", item.get("Text"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
