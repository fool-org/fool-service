package org.fool.framework.view.service;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.model.View;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataQueryServiceDetailTest {
    @Test
    public void queryLegacyViewDataDetailLoadsViewObjectAndFormatsDetail() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "1001");

        assertSame(expected, actual);
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void initLegacyNewObjectFormatsEmptyDetailAndKeepsParentId() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        QueryDataDetailResult expected = new QueryDataDetailResult();
        QueryDataDetailResult.DataDetail detail = new QueryDataDetailResult.DataDetail();
        expected.setData(detail);
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);
        when(viewAdapter.getDetailViewResult(view, null)).thenReturn(expected);

        QueryDataDetailResult actual = service.initLegacyNewObject("100", "5001");

        assertSame(expected, actual);
        assertEquals("5001", actual.getData().getParentId());
        verify(viewAdapter).getDetailViewResult(view, null);
    }
}
