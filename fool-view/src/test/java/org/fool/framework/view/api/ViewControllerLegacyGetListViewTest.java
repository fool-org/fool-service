package org.fool.framework.view.api;

import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.adapter.ViewAdapter;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.ViewDataRequest;
import org.fool.framework.view.model.View;
import org.fool.framework.view.service.ViewDataService;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewControllerLegacyGetListViewTest {
    @Test
    public void getListViewMapsLegacyViewIdPayload() throws Exception {
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewAdapter viewAdapter = mock(ViewAdapter.class);
        View view = new View();
        ListViewInfo expected = new ListViewInfo();
        when(viewDataService.getViewData("100", "token-1")).thenReturn(view);
        when(viewAdapter.getViewInfo(view)).thenReturn(expected);

        ViewController controller = new ViewController();
        setField(controller, "viewDataService", viewDataService);
        setField(controller, "viewAdapter", viewAdapter);
        ViewDataRequest request = new ViewDataRequest();
        request.setToken("token-1");
        request.setViewId(100L);

        CommonResponse<ListViewInfo> response = controller.getListView(request);

        verify(viewDataService).getViewData("100", "token-1");
        assertEquals(0, response.getCode());
        assertSame(expected, response.getData());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
