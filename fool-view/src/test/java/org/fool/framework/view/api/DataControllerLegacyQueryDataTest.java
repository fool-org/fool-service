package org.fool.framework.view.api;

import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.LegacyQueryDataRequest;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataControllerLegacyQueryDataTest {
    @Test
    public void queryDataMapsLegacyViewIdPagingAndFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        ListViewResult expected = new ListViewResult();
        when(dataQueryService.queryLegacyViewData(eq("42"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_state`='OPEN'")))
                .thenReturn(expected);

        DataController controller = new DataController();
        setField(controller, "dataQueryService", dataQueryService);
        LegacyQueryDataRequest request = new LegacyQueryDataRequest();
        request.setViewId(42L);
        request.setPageSize(20);
        request.setPageIndex(3);
        request.setQueryFilter("`order_state`='OPEN'");

        CommonResponse<ListViewResult> response = controller.queryData(request);

        ArgumentCaptor<PageNavigator> pageCaptor = ArgumentCaptor.forClass(PageNavigator.class);
        verify(dataQueryService).queryLegacyViewData(eq("42"), pageCaptor.capture(), eq("`order_state`='OPEN'"));
        assertEquals(0, response.getCode());
        assertSame(expected, response.getData());
        assertEquals(20, pageCaptor.getValue().getPageSize());
        assertEquals(3, pageCaptor.getValue().getPageIndex());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
