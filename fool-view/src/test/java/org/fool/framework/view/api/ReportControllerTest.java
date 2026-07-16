package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.common.PropertyType;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.JdbcCompareOpCatalog;
import org.fool.framework.query.JdbcSelectTypeCatalog;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.LegacyCompareOp;
import org.fool.framework.query.SelectType;
import org.fool.framework.report.ReportCell;
import org.fool.framework.report.ReportGridResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.MakeReportRequest;
import org.fool.framework.view.dto.ReportModelResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReportControllerTest {
    @Test
    public void getReportModelMapsLegacyCandidateColumns() throws Exception {
        DaoService daoService = mock(DaoService.class);
        JdbcCompareOpCatalog compareOpCatalog = mock(JdbcCompareOpCatalog.class);
        JdbcSelectTypeCatalog selectTypeCatalog = mock(JdbcSelectTypeCatalog.class);
        Model orderState = new Model();
        orderState.setId(102L);
        orderState.setEnumValues(List.of(enumValue("Open", "0"), enumValue("Filled", "1")));
        Property symbol = property(1002L, "symbol", "order_symbol", PropertyType.String);
        symbol.setRemark("Symbol");
        Property state = property(1003L, "state", "order_state", PropertyType.Enum);
        state.setRemark("State");
        state.setPropertyModel(orderState);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(symbol, state));
        when(compareOpCatalog.listFor(PropertyType.String)).thenReturn(List.of(compareOp(1, "等于"), compareOp(7, "包含")));
        when(selectTypeCatalog.listFor(PropertyType.String)).thenReturn(List.of(selectType(1, "原值")));
        when(compareOpCatalog.listFor(PropertyType.Enum)).thenReturn(List.of(compareOp(1, "等于")));
        when(selectTypeCatalog.listFor(PropertyType.Enum)).thenReturn(List.of(selectType(1, "原值")));

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "daoService", daoService);
        setField(controller, "compareOpCatalog", compareOpCatalog);
        setField(controller, "selectTypeCatalog", selectTypeCatalog);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);

        CommonResponse<ReportModelResult> response = controller.getReportModel(request);

        assertEquals(0, response.getCode());
        ReportModelResult.QueryCol symbolCol = response.getData().getCols().get(0);
        assertEquals("symbol", symbolCol.getId());
        assertEquals("Symbol", symbolCol.getName());
        assertEquals(Integer.valueOf(PropertyType.String.code()), symbolCol.getPrpType());
        assertEquals("1", symbolCol.getCompareTypes().get(0).getId());
        assertEquals("包含", symbolCol.getCompareTypes().get(1).getName());
        assertEquals("原值", symbolCol.getQueryTypes().get(0).getName());
        ReportModelResult.QueryCol stateCol = response.getData().getCols().get(1);
        assertEquals(Long.valueOf(102L), stateCol.getModelId());
        assertEquals("Open", stateCol.getStates().get(0).getShowName());
        assertEquals("0", stateCol.getStates().get(0).getDbName());
    }

    @Test
    public void getReportModelUsesConfiguredViewItemsInsteadOfAllModelProperties() throws Exception {
        DaoService daoService = mock(DaoService.class);
        JdbcCompareOpCatalog compareOpCatalog = mock(JdbcCompareOpCatalog.class);
        JdbcSelectTypeCatalog selectTypeCatalog = mock(JdbcSelectTypeCatalog.class);
        Property symbol = property(1002L, "symbol", "order_symbol", PropertyType.String);
        Property state = property(1003L, "state", "order_state", PropertyType.Enum);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100", viewItem("Pair", "symbol")));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(symbol, state));
        when(compareOpCatalog.listFor(PropertyType.String)).thenReturn(List.of(compareOp(1, "等于")));
        when(selectTypeCatalog.listFor(PropertyType.String)).thenReturn(List.of(selectType(1, "原值")));

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "daoService", daoService);
        setField(controller, "compareOpCatalog", compareOpCatalog);
        setField(controller, "selectTypeCatalog", selectTypeCatalog);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);

        CommonResponse<ReportModelResult> response = controller.getReportModel(request);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().getCols().size());
        assertEquals("symbol", response.getData().getCols().get(0).getId());
        assertEquals("Pair", response.getData().getCols().get(0).getName());
    }

    @Test
    public void getReportModelOrdersConfiguredViewItemsByShowIndex() throws Exception {
        DaoService daoService = mock(DaoService.class);
        Property symbol = property(1002L, "symbol", "order_symbol", PropertyType.String);
        Property state = property(1003L, "state", "order_state", PropertyType.String);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100",
                viewItem("State", "state", 20),
                viewItem("Symbol", "symbol", 10)));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(symbol, state));

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "daoService", daoService);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);

        CommonResponse<ReportModelResult> response = controller.getReportModel(request);

        assertEquals("Symbol", response.getData().getCols().get(0).getName());
        assertEquals("State", response.getData().getCols().get(1).getName());
    }

    @Test
    public void getReportModelExposesLegacyPascalAliases() {
        ReportModelResult result = new ReportModelResult();
        ReportModelResult.QueryCol col = new ReportModelResult.QueryCol();
        col.setId("symbol");
        col.setName("Symbol");
        col.setPrpType(PropertyType.String.code());
        col.setModelId(102L);
        col.setCompareTypes(List.of(new ReportModelResult.Option("7", "包含")));
        col.setQueryTypes(List.of(new ReportModelResult.Option("1", "原值")));
        ReportModelResult.StateValue state = new ReportModelResult.StateValue();
        state.setShowName("Open");
        state.setDbName("0");
        col.setStates(List.of(state));
        result.setCols(List.of(col));

        Map<?, ?> serialized = new ObjectMapper().convertValue(result, Map.class);
        assertTrue(serialized.containsKey("cols"));
        assertTrue(serialized.containsKey("Cols"));
        assertEquals(serialized.get("cols"), serialized.get("Cols"));
        Map<?, ?> item = (Map<?, ?>) ((List<?>) serialized.get("Cols")).get(0);
        assertEquals("symbol", item.get("id"));
        assertEquals("symbol", item.get("ID"));
        assertEquals("Symbol", item.get("Name"));
        assertEquals(PropertyType.String.code(), item.get("PrpType"));
        assertEquals(102L, item.get("ModelId"));
        assertTrue(item.containsKey("CompareTypes"));
        assertTrue(item.containsKey("QueryTypes"));
        assertTrue(item.containsKey("States"));
    }

    @Test
    public void makeReportExposesLegacyPascalGridAliases() {
        ReportGridResult result = new ReportGridResult();
        result.setViewId(100);
        result.setCurrentPage(1);
        result.setPageSize(10);
        result.setTotalRecords(1);
        result.setTotalPages(1);
        ReportCell cell = new ReportCell();
        cell.setCol(0);
        cell.setRow(1);
        cell.setColSpan(1);
        cell.setRowSpan(1);
        cell.setFmtValue("BTC-USDT");
        result.setCells(List.of(cell));

        Map<?, ?> serialized = new ObjectMapper().convertValue(result, Map.class);
        assertEquals(100, serialized.get("ViewId"));
        assertEquals(1L, serialized.get("TotalRecords"));
        assertTrue(serialized.containsKey("Cells"));
        assertEquals(serialized.get("cells"), serialized.get("Cells"));
        Map<?, ?> item = (Map<?, ?>) ((List<?>) serialized.get("Cells")).get(0);
        assertEquals(0, item.get("Col"));
        assertEquals(1, item.get("Row"));
        assertEquals(1, item.get("ColSpan"));
        assertEquals(1, item.get("RowSpan"));
        assertEquals("BTC-USDT", item.get("FmtValue"));
    }

    @Test
    public void makeReportRejectsLegacyRawQueryFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setTotalItem(42L);
        queryResult.setTotalPage(3L);
        queryResult.setData(List.of(row()));
        stubReportQuery(dataQueryService, "100", "order_state=\"0\"", queryResult);

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setCurrentPage(2);
        request.setPageSize(20);
        request.setQueryFilter("order_state=\"0\"");

        AuthorizationDeniedException exception = assertThrows(
                AuthorizationDeniedException.class,
                () -> controller.makeReport(request));

        assertEquals("CLIENT_RAW_FILTER_FORBIDDEN", exception.getMessage());
    }

    @Test
    public void makeReportOrdersRowsByFirstReportColumnWhenOrderTypeIsNull() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setTotalItem(2L);
        queryResult.setTotalPage(1L);
        queryResult.setData(List.of(row("Beta", "Open"), row("Alpha", "Open")));
        stubReportQuery(dataQueryService, "100", null, queryResult);

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest.ReportCol symbol = reportCol("Symbol", "Symbol", 0);
        symbol.setOrderType("2");
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setReportCols(List.of(symbol));

        controller.makeReport(request);

        verify(dataQueryService).queryReportViewData(
                eq("100"),
                org.mockito.ArgumentMatchers.any(PageNavigator.class),
                eq(null),
                eq(new DataQueryService.QueryOrder("Symbol", false)));

        symbol.setOrderType("1");
        controller.makeReport(request);

        verify(dataQueryService).queryReportViewData(
                eq("100"),
                org.mockito.ArgumentMatchers.any(PageNavigator.class),
                eq(null),
                eq(new DataQueryService.QueryOrder("Symbol", true)));
    }

    @Test
    public void makeReportPassesMultipleReportOrdersBySelectedIndex() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setData(List.of(row()));
        stubReportQuery(dataQueryService, "100", null, queryResult);

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest.ReportCol symbol = reportCol("Symbol", "Symbol", 2);
        symbol.setOrderType("0");
        MakeReportRequest.ReportCol state = reportCol("State", "State", 1);
        state.setOrderType("1");
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setReportCols(List.of(symbol, state));

        controller.makeReport(request);

        verify(dataQueryService).queryReportViewData(
                eq("100"),
                org.mockito.ArgumentMatchers.any(PageNavigator.class),
                eq(null),
                eq(new DataQueryService.QueryOrder(List.of(
                        new DataQueryService.QueryOrder.Item("State", true),
                        new DataQueryService.QueryOrder.Item("Symbol", false)))));
    }

    @Test
    public void makeReportAlsoExposesLegacyGetRptRoute() throws Exception {
        var mapping = ReportController.class
                .getMethod("makeReport", MakeReportRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertEquals(true, List.of(mapping.value()).contains("/getrpt"));
        assertEquals(true, List.of(mapping.value()).contains("/mkrpt"));
    }

    @Test
    public void getReportModelAlsoExposesLegacyMkqviewRoute() throws Exception {
        var mapping = ReportController.class
                .getMethod("getReportModel", MakeReportRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/mkqview"));
    }

    @Test
    public void makeReportAcceptsLegacyWebMkrptPayloadAliases() throws Exception {
        MakeReportRequest request = new ObjectMapper().readValue("""
                {
                  "viewid": 100,
                  "cols": [{"ColName": "State", "ColId": "state", "SelectedTypeId": "1", "Index": 2, "OrderType": "0"}],
                  "pagesize": 10,
                  "pageindex": 2,
                  "exp": {"Col": {"Name": "order_state"}, "CompareOp": {"ID": "1", "Name": "等于"}, "ValueExp": "0", "ValueFmt": "Open"},
                  "reportname": "Order Daily"
                }
                """, MakeReportRequest.class);

        assertEquals(Long.valueOf(100L), request.getViewId());
        assertEquals(Integer.valueOf(10), request.getPageSize());
        assertEquals(Integer.valueOf(2), request.getCurrentPage());
        assertEquals("Order Daily", request.getReportName());
        assertEquals("State", request.getReportCols().get(0).getColName());
        assertEquals("state", request.getReportCols().get(0).getColId());
        assertEquals("1", request.getReportCols().get(0).getSelectedTypeId());
        assertEquals("0", request.getReportCols().get(0).getOrderType());
        assertEquals("order_state", request.getFilterExp().getCol().getName());
        assertEquals("1", request.getFilterExp().getCompareOp().getId());
        assertEquals("0", request.getFilterExp().getValueExp());
    }

    @Test
    public void makeReportResolvesReportColIdThroughViewModelMetadata() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setData(List.of(row()));
        stubReportQuery(dataQueryService, "100", null, queryResult);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        Property symbol = property(1002L, "symbol", "order_symbol", PropertyType.String);
        symbol.setRemark("Symbol");
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(symbol));

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setCurrentPage(1);
        request.setPageSize(10);
        request.setReportCols(List.of(reportColId("symbol", 1)));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getCells().size());
        assertCell(response.getData().getCells().get(0), 0, 0, "Symbol");
        assertCell(response.getData().getCells().get(1), 0, 1, "BTC-USDT");
    }

    @Test
    public void makeReportProjectsColIdValuesToCustomReportColName() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setData(List.of(row()));
        stubReportQuery(dataQueryService, "100", null, queryResult);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100")))
                .thenReturn(model(property(1002L, "symbol", "order_symbol", PropertyType.String)));

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setCurrentPage(1);
        request.setPageSize(10);
        request.setReportCols(List.of(reportCol("Pair", "symbol", 1)));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertEquals(0, response.getCode());
        assertCell(response.getData().getCells().get(0), 0, 0, "Pair");
        assertCell(response.getData().getCells().get(1), 0, 1, "BTC-USDT");
    }

    @Test
    public void makeReportRejectsUnknownRequestedColumn() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        ListDataItem row = row();
        row.setValues(Map.of("DTO Only", "from-values"));
        ListViewResult queryResult = new ListViewResult();
        queryResult.setData(List.of(row));
        stubReportQuery(dataQueryService, "100", null, queryResult);

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setReportCols(List.of(reportCol("DTO Only", 1), reportCol("Symbol", 2)));

        AuthorizationDeniedException exception = assertThrows(
                AuthorizationDeniedException.class,
                () -> controller.makeReport(request));

        assertEquals("FIELD_NOT_READABLE", exception.getMessage());
    }

    @Test
    public void makeReportUsesViewColumnForSingleCountSelectedType() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        JdbcSelectTypeCatalog selectTypeCatalog = mock(JdbcSelectTypeCatalog.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setTotalItem(42L);
        stubReportQuery(dataQueryService, "100", null, queryResult);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100")))
                .thenReturn(view("asset-model", viewItem("Asset Code", "assetCode", 10)));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("asset-model")))
                .thenReturn(model(property(2002L, "assetCode", "asset_code", PropertyType.String)));
        when(selectTypeCatalog.listFor(PropertyType.String)).thenReturn(List.of(selectType(2, "计数", "COUNT({0})")));

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);
        setField(controller, "selectTypeCatalog", selectTypeCatalog);

        MakeReportRequest.ReportCol count = reportCol("Asset Code[计数]", "assetCode", 0);
        count.setSelectedTypeId("2");
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setReportCols(List.of(count));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertEquals(0, response.getCode());
        assertEquals(1, response.getData().getTotalRecords());
        assertEquals(1, response.getData().getTotalPages());
        assertCell(response.getData().getCells().get(0), 0, 0, "Asset Code[计数]");
        assertCell(response.getData().getCells().get(1), 0, 1, "42");
    }

    @Test
    public void saveReportKeepsLegacyNoOpSuccessSurface() {
        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setReportName("Order Daily");
        request.setReportCols(List.of(reportCol("Symbol", 1)));
        request.setFilterExp(filterExp("order_state", "1", "等于", "0", "Open"));

        CommonResponse<Void> response = controller.saveReport(request);

        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsLegacySimpleFilterExpToQueryFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        stubReportQuery(dataQueryService, "100", "`order_state`='0'", new ListViewResult());

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setCurrentPage(1);
        request.setPageSize(10);
        request.setFilterExp(filterExp("order_state", "1", "等于", "0", "Open"));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertFilter(dataQueryService, "100", "`order_state`=?", "0");
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsViewItemNameFilterExpToQueryFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100")))
                .thenReturn(view("asset-model", viewItem("Asset Code", "assetCode", 10)));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("asset-model")))
                .thenReturn(model(property("assetCode", "asset_code")));
        stubReportQuery(dataQueryService, "100", "`asset_code` LIKE '%Bond%'", new ListViewResult());

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(filterExp("Asset Code", "7", "包含", "Bond", "Bond"));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertFilter(dataQueryService, "100", "`asset_code` LIKE ?", "%Bond%");
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsLegacyFilterExpColumnIdToModelColumn() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(property(11L, "price", "order_price")));
        stubReportQuery(dataQueryService, "100", "`order_price`>'100'", new ListViewResult());

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(filterExpById("11", "3", "大于", "100", "100"));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertFilter(dataQueryService, "100", "`order_price`>?", "100");
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsLegacyCompositeFilterExpToQueryFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(property("symbol", "order_symbol")));
        stubReportQuery(dataQueryService, "100",
                "(`order_state`='0') And (`order_symbol` LIKE '%BTC%') OR (`order_price`>'100')",
                new ListViewResult());

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(compositeFilterExp(
                filterExp("order_state", "1", "等于", "0", "Open"),
                seq("and", "与", filterExp("symbol", "7", "包含", "BTC", "BTC")),
                seq("or", "或", filterExp("order_price", "3", "大于", "100", "100"))));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertFilter(dataQueryService, "100",
                "(`order_state`=?) And (`order_symbol` LIKE ?) OR (`order_price`>?)",
                "0", "%BTC%", "100");
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportKeepsNestedCompositeFilterParentheses() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(property("symbol", "order_symbol")));
        stubReportQuery(dataQueryService, "100",
                "((`order_state`='0') And (`order_symbol` LIKE '%BTC%')) OR (`order_price`>'100')",
                new ListViewResult());

        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(compositeFilterExp(
                compositeFilterExp(
                        filterExp("order_state", "1", "等于", "0", "Open"),
                        seq("and", "与", filterExp("symbol", "7", "包含", "BTC", "BTC"))),
                seq("or", "或", filterExp("order_price", "3", "大于", "100", "100"))));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        assertFilter(dataQueryService, "100",
                "(`order_state`=?) And (`order_symbol` LIKE ?) OR (`order_price`>?)",
                "0", "%BTC%", "100");
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportRejectsUnknownFilterExpInsteadOfIgnoringIt() throws Exception {
        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(filterExp("order_state", "99", "自定义", "0", "Open"));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(request));
    }

    @Test
    public void makeReportRejectsUnknownCompositeBoolOp() throws Exception {
        ReportController controller = new ReportController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(compositeFilterExp(
                filterExp("order_state", "1", "等于", "0", "Open"),
                seq("or 1=1", "or 1=1", filterExp("symbol", "7", "包含", "BTC", "BTC"))));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(request));

        MakeReportRequest emptyBoolOpRequest = new MakeReportRequest();
        emptyBoolOpRequest.setViewId(100L);
        emptyBoolOpRequest.setFilterExp(compositeFilterExp(
                filterExp("order_state", "1", "等于", "0", "Open"),
                seq(null, null, filterExp("symbol", "7", "包含", "BTC", "BTC"))));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(emptyBoolOpRequest));
    }

    private static ListDataItem row() {
        return row("BTC-USDT", "Open");
    }

    private static ListDataItem row(String symbol, String state) {
        ListDataItem row = new ListDataItem();
        row.setItems(List.of(value("symbol", "Symbol", symbol), value("state", "State", state)));
        return row;
    }

    private static void stubReportQuery(
            DataQueryService dataQueryService,
            String viewId,
            String filter,
            ListViewResult result) {
        when(dataQueryService.queryReportViewData(
                eq(viewId),
                org.mockito.ArgumentMatchers.any(PageNavigator.class),
                org.mockito.ArgumentMatchers.nullable(IQueryFilter.class),
                org.mockito.ArgumentMatchers.nullable(DataQueryService.QueryOrder.class)))
                .thenReturn(result);
    }

    private static void assertFilter(
            DataQueryService dataQueryService,
            String viewId,
            String sql,
            Object... args) {
        ArgumentCaptor<IQueryFilter> filter = ArgumentCaptor.forClass(IQueryFilter.class);
        verify(dataQueryService).queryReportViewData(
                eq(viewId),
                org.mockito.ArgumentMatchers.any(PageNavigator.class),
                filter.capture(),
                org.mockito.ArgumentMatchers.nullable(DataQueryService.QueryOrder.class));
        QueryAndArgs query = filter.getValue().generateSql();
        assertEquals(sql, query.getSql());
        assertArrayEquals(args, query.getArgs());
    }

    private static ListDataValue value(String id, String name, String value) {
        ListDataValue dataValue = new ListDataValue();
        dataValue.setPrpId(id);
        dataValue.setPrpShowName(name);
        dataValue.setFmtValue(value);
        return dataValue;
    }

    private static MakeReportRequest.ReportCol reportCol(String name, int index) {
        MakeReportRequest.ReportCol col = new MakeReportRequest.ReportCol();
        col.setColName(name);
        col.setIndex(index);
        return col;
    }

    private static MakeReportRequest.ReportCol reportCol(String name, String id, int index) {
        MakeReportRequest.ReportCol col = reportCol(name, index);
        col.setColId(id);
        return col;
    }

    private static MakeReportRequest.ReportCol reportColId(String id, int index) {
        MakeReportRequest.ReportCol col = new MakeReportRequest.ReportCol();
        col.setColId(id);
        col.setIndex(index);
        return col;
    }

    private static MakeReportRequest.BoolExp filterExp(String columnName, String compareId, String compareName, String value, String fmtValue) {
        MakeReportRequest.QueryCol col = new MakeReportRequest.QueryCol();
        col.setName(columnName);
        MakeReportRequest.CompareOpItem compare = new MakeReportRequest.CompareOpItem();
        compare.setId(compareId);
        compare.setName(compareName);
        MakeReportRequest.BoolExp exp = new MakeReportRequest.BoolExp();
        exp.setCol(col);
        exp.setCompareOp(compare);
        exp.setValueExp(value);
        exp.setValueFmt(fmtValue);
        return exp;
    }

    private static MakeReportRequest.BoolExp compositeFilterExp(MakeReportRequest.BoolExp first, MakeReportRequest.AddBoolExp... sequences) {
        MakeReportRequest.BoolExp exp = new MakeReportRequest.BoolExp();
        exp.setFirstExp(first);
        exp.setSequences(List.of(sequences));
        return exp;
    }

    private static MakeReportRequest.AddBoolExp seq(String dbName, String showName, MakeReportRequest.BoolExp addedExp) {
        MakeReportRequest.BoolOp boolOp = new MakeReportRequest.BoolOp();
        boolOp.setDbName(dbName);
        boolOp.setShowName(showName);
        MakeReportRequest.AddBoolExp seq = new MakeReportRequest.AddBoolExp();
        seq.setBoolOp(boolOp);
        seq.setAddedExp(addedExp);
        return seq;
    }

    private static MakeReportRequest.BoolExp filterExpById(String columnId, String compareId, String compareName, String value, String fmtValue) {
        MakeReportRequest.QueryCol col = new MakeReportRequest.QueryCol();
        col.setId(columnId);
        MakeReportRequest.CompareOpItem compare = new MakeReportRequest.CompareOpItem();
        compare.setId(compareId);
        compare.setName(compareName);
        MakeReportRequest.BoolExp exp = new MakeReportRequest.BoolExp();
        exp.setCol(col);
        exp.setCompareOp(compare);
        exp.setValueExp(value);
        exp.setValueFmt(fmtValue);
        return exp;
    }

    private static View view(String modelId) {
        View view = new View();
        view.setViewModel(modelId);
        return view;
    }

    private static View view(String modelId, ViewItem... items) {
        View view = view(modelId);
        view.setListItems(List.of(items));
        return view;
    }

    private static ViewItem viewItem(String itemName, String modelProperty) {
        ViewItem item = new ViewItem();
        item.setItemName(itemName);
        item.setModelProperty(modelProperty);
        return item;
    }

    private static ViewItem viewItem(String itemName, String modelProperty, int showIndex) {
        ViewItem item = viewItem(itemName, modelProperty);
        item.setShowIndex(showIndex);
        return item;
    }

    private static Model model(Property... properties) {
        Model model = new Model();
        model.setProperties(List.of(properties));
        return model;
    }

    private static Property property(String name, String column) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        return property;
    }

    private static Property property(Long id, String name, String column) {
        Property property = property(name, column);
        property.setId(id);
        return property;
    }

    private static Property property(Long id, String name, String column, PropertyType propertyType) {
        Property property = property(id, name, column);
        property.setPropertyType(propertyType);
        return property;
    }

    private static EnumValue enumValue(String name, String value) {
        EnumValue enumValue = new EnumValue();
        enumValue.setName(name);
        enumValue.setValue(value);
        return enumValue;
    }

    private static LegacyCompareOp compareOp(long id, String name) {
        LegacyCompareOp op = new LegacyCompareOp();
        op.setId(id);
        op.setShowName(name);
        return op;
    }

    private static SelectType selectType(long id, String name) {
        SelectType type = new SelectType();
        type.setId(id);
        type.setShow(name);
        return type;
    }

    private static SelectType selectType(long id, String name, String dbExp) {
        SelectType type = selectType(id, name);
        type.setDbExp(dbExp);
        return type;
    }

    private static void assertCell(ReportCell cell, int col, int row, String value) {
        assertEquals(col, cell.getCol());
        assertEquals(row, cell.getRow());
        assertEquals(1, cell.getColSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals(value, cell.getFmtValue());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

}
