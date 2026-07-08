package org.fool.framework.view.service;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.common.context.LegacyContextValueService;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.model.View;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataQueryServiceDetailTest {
    @Test
    public void queryLegacyViewDataDetailLoadsViewObjectAndFormatsDetail() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "1001");

        assertSame(expected, actual);
        InOrder inOrder = inOrder(viewDataService, modelDataService);
        inOrder.verify(viewDataService).getViewData("100", null);
        inOrder.verify(modelDataService).getModel("Order");
        inOrder.verify(modelDataService).getOneData("Order", "1001");
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void queryLegacyViewDataDetailPassesTokenToViewLookup() {
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100", "token-1")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "1001", null, "token-1");

        assertSame(expected, actual);
        verify(viewDataService).getViewData("100", "token-1");
    }

    @Test
    public void queryLegacyViewDataDetailUsesStaticIdExpressionWhenObjectIdIsBlank() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", " ", "$1001");

        assertSame(expected, actual);
        verify(modelDataService).getOneData("Order", "1001");
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void queryLegacyViewDataDetailEvaluatesIdExpressionMath() {
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "", "$1000+$1");

        assertSame(expected, actual);
        verify(modelDataService).getOneData("Order", "1001");
    }

    @Test
    public void queryLegacyViewDataDetailResolvesContextIdExpressionFromToken() {
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        LegacyContextValueService contextValueService = mock(LegacyContextValueService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);
        ReflectionTestUtils.setField(service, "contextValueService", contextValueService);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100", "token-1")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(contextValueService.getValue("token-1", "userid")).thenReturn("admin");
        when(modelDataService.getOneData("Order", "admin")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "", "@userid", "token-1");

        assertSame(expected, actual);
        verify(modelDataService).getOneData("Order", "admin");
    }

    @Test
    public void queryLegacyViewDataDetailUsesFirstResultWhenObjectIdAndIdExpressionAreBlank() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        model.setProperties(List.of());
        IDynamicData first = mock(IDynamicData.class);
        IDynamicData data = mock(IDynamicData.class);
        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(first));
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(first.getId()).thenReturn("1001");
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"), any(IQueryFilter.class), eq(model.getProperties()), any(PageNavigator.class)))
                .thenReturn(page);
        when(modelDataService.getOneData("Order", "1001")).thenReturn(data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "", "");

        assertSame(expected, actual);
        verify(modelDataService).getOneData("Order", "1001");
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void initLegacyNewObjectFormatsEmptyDetailAndKeepsParentId() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = new Model();
        QueryDataDetailResult expected = new QueryDataDetailResult();
        QueryDataDetailResult.DataDetail detail = new QueryDataDetailResult.DataDetail();
        expected.setData(detail);
        when(viewDataService.getViewData("100", null)).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(viewAdapter.getDetailViewResult(view, null)).thenReturn(expected);

        QueryDataDetailResult actual = service.initLegacyNewObject("100", "5001");

        assertSame(expected, actual);
        assertEquals("5001", actual.getData().getParentId());
        verify(viewDataService).getViewData("100", null);
        verify(viewAdapter).getDetailViewResult(view, null);
    }
}
