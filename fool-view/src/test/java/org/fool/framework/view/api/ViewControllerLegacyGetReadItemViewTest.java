package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.dto.CommonException;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.adapter.ViewAdapter;
import org.fool.framework.view.dto.ReadItemViewInfo;
import org.fool.framework.view.dto.ViewDataRequest;
import org.fool.framework.view.model.View;
import org.fool.framework.view.service.ViewDataService;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewControllerLegacyGetReadItemViewTest {
    @Test
    public void getReadItemViewMapsLegacyViewIdPayload() throws Exception {
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewAdapter viewAdapter = mock(ViewAdapter.class);
        View view = new View();
        ReadItemViewInfo expected = new ReadItemViewInfo();
        when(viewDataService.getViewData("200")).thenReturn(view);
        when(viewAdapter.getReadItemView(eq(view), any())).thenReturn(expected);

        ViewController controller = new ViewController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "viewDataService", viewDataService);
        setField(controller, "viewAdapter", viewAdapter);
        ViewDataRequest request = new ViewDataRequest();
        request.setViewId(200L);

        CommonResponse<ReadItemViewInfo> response = controller.getReadItemView(request);

        verify(viewDataService).getViewData("200");
        verify(viewAdapter).getReadItemView(eq(view), any());
        assertEquals(0, response.getCode());
        assertSame(expected, response.getData());
    }

    @Test
    public void getReadItemViewRejectsViewNameOnlyRequest() throws Exception {
        ViewController controller = new ViewController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "viewDataService", mock(ViewDataService.class));
        setField(controller, "viewAdapter", mock(ViewAdapter.class));
        ViewDataRequest request = new ObjectMapper().readValue(
                "{\"ViewName\":\"BusinessNameShortcut\"}",
                ViewDataRequest.class);

        CommonException exception = assertThrows(CommonException.class, () -> controller.getReadItemView(request));

        assertEquals("ViewId is required", exception.getMessage());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
