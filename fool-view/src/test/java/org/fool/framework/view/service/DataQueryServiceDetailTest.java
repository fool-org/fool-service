package org.fool.framework.view.service;

import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.common.context.LegacyContextValueService;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
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
        org.fool.framework.view.TestReadAuthorization.install(service);
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        stubDetailQuery(modelDataService, data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "1001");

        assertSame(expected, actual);
        InOrder inOrder = inOrder(viewDataService, modelDataService);
        inOrder.verify(viewDataService).getViewData("100");
        inOrder.verify(modelDataService).getModel("Order");
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void queryLegacyViewDataDetailUsesServerOwnedContext() {
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        org.fool.framework.view.TestReadAuthorization.install(service);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        stubDetailQuery(modelDataService, data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "1001", null);

        assertSame(expected, actual);
        verify(viewDataService).getViewData("100");
    }

    @Test
    public void queryLegacyViewDataDetailUsesStaticIdExpressionWhenObjectIdIsBlank() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        org.fool.framework.view.TestReadAuthorization.install(service);
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        stubDetailQuery(modelDataService, data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", " ", "$1001");

        assertSame(expected, actual);
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void queryLegacyViewDataDetailEvaluatesIdExpressionMath() {
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        org.fool.framework.view.TestReadAuthorization.install(service);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        stubDetailQuery(modelDataService, data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "", "$1000+$1");

        assertSame(expected, actual);
    }

    @Test
    public void queryLegacyViewDataDetailResolvesContextFromEffectiveSubject() {
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        LegacyContextValueService contextValueService = mock(LegacyContextValueService.class);
        DataQueryService service = new DataQueryService();
        org.fool.framework.view.TestReadAuthorization.install(service);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);
        ReflectionTestUtils.setField(service, "contextValueService", contextValueService);

        View view = new View();
        view.setViewModel("Order");
        Model model = model();
        IDynamicData data = mock(IDynamicData.class);
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(contextValueService.getValue(null, "userid")).thenReturn("admin");
        stubDetailQuery(modelDataService, data);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "", "@userid");

        assertSame(expected, actual);
        verify(contextValueService).getValue(null, "userid");
    }

    @Test
    public void queryLegacyViewDataDetailUsesFirstResultWhenObjectIdAndIdExpressionAreBlank() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        org.fool.framework.view.TestReadAuthorization.install(service);
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewDataService", viewDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", viewAdapter);

        View view = new View();
        view.setViewModel("Order");
        Model model = model();
        IDynamicData first = mock(IDynamicData.class);
        IDynamicData data = mock(IDynamicData.class);
        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(first));
        QueryDataDetailResult expected = new QueryDataDetailResult();
        when(first.get("SYSID")).thenReturn("1001");
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        PageResult<IDynamicData> authorized = new PageResult<>();
        authorized.setItems(List.of(data));
        when(modelDataService.getDataListWithPageInfo(
                eq("Order"), any(IQueryFilter.class), any(), any(PageNavigator.class)))
                .thenReturn(page, authorized);
        when(viewAdapter.getDetailViewResult(view, data)).thenReturn(expected);

        QueryDataDetailResult actual = service.queryLegacyViewDataDetail("100", "", "");

        assertSame(expected, actual);
        verify(viewAdapter).getDetailViewResult(view, data);
    }

    @Test
    public void initLegacyNewObjectFormatsEmptyDetailAndKeepsParentId() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        ViewDataService viewDataService = mock(ViewDataService.class);
        ViewDataAdapter viewAdapter = mock(ViewDataAdapter.class);
        DataQueryService service = new DataQueryService();
        org.fool.framework.view.TestReadAuthorization.install(service);
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
        when(viewDataService.getViewData("100")).thenReturn(view);
        when(modelDataService.getModel("Order")).thenReturn(model);
        when(viewAdapter.getDetailViewResult(view, null)).thenReturn(expected);

        QueryDataDetailResult actual = service.initLegacyNewObject("100", "5001");

        assertSame(expected, actual);
        assertEquals("5001", actual.getData().getParentId());
        verify(viewDataService).getViewData("100");
        verify(viewAdapter).getDetailViewResult(view, null);
    }

    private static Model model() {
        Property id = new Property();
        id.setName("orderId");
        id.setColumn("ORDER_ID");
        Model model = new Model();
        model.setProperties(List.of(id));
        model.setIdProperty(id);
        return model;
    }

    private static void stubDetailQuery(ModelDataService service, IDynamicData data) {
        PageResult<IDynamicData> page = new PageResult<>();
        page.setItems(List.of(data));
        when(service.getDataListWithPageInfo(
                eq("Order"), any(IQueryFilter.class), any(), any(PageNavigator.class)))
                .thenReturn(page);
    }
}
