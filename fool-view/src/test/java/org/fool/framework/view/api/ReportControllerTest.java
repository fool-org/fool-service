package org.fool.framework.view.api;

import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.DaoService;
import org.fool.framework.common.PropertyType;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.JdbcCompareOpCatalog;
import org.fool.framework.query.JdbcSelectTypeCatalog;
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
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
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
        setField(controller, "daoService", daoService);
        setField(controller, "compareOpCatalog", compareOpCatalog);
        setField(controller, "selectTypeCatalog", selectTypeCatalog);
        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);

        CommonResponse<ReportModelResult> response = controller.getReportModel(request);

        assertEquals(0, response.getCode());
        ReportModelResult.QueryCol symbolCol = response.getData().getCols().get(0);
        assertEquals("1002", symbolCol.getId());
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
    public void makeReportMapsLegacyQueryDataIntoReportGridCells() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        ListViewResult queryResult = new ListViewResult();
        queryResult.setTotalItem(42L);
        queryResult.setTotalPage(3L);
        queryResult.setData(List.of(row()));
        when(dataQueryService.queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("order_state=\"0\"")))
                .thenReturn(queryResult);

        ReportController controller = new ReportController();
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setCurrentPage(2);
        request.setPageSize(20);
        request.setQueryFilter("order_state=\"0\"");
        request.setReportCols(List.of(reportCol("State", 2), reportCol("Symbol", 1)));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        ArgumentCaptor<PageNavigator> pageCaptor = ArgumentCaptor.forClass(PageNavigator.class);
        verify(dataQueryService).queryLegacyViewData(eq("100"), pageCaptor.capture(), eq("order_state=\"0\""));
        assertEquals(20, pageCaptor.getValue().getPageSize());
        assertEquals(2, pageCaptor.getValue().getPageIndex());
        assertEquals(0, response.getCode());

        ReportGridResult result = response.getData();
        assertEquals(100, result.getViewId());
        assertEquals(2, result.getCurrentPage());
        assertEquals(20, result.getPageSize());
        assertEquals(42, result.getTotalRecords());
        assertEquals(3, result.getTotalPages());
        assertCell(result.getCells().get(0), 0, 0, "Symbol");
        assertCell(result.getCells().get(1), 1, 0, "State");
        assertCell(result.getCells().get(2), 0, 1, "BTC-USDT");
        assertCell(result.getCells().get(3), 1, 1, "Open");
    }

    @Test
    public void saveReportKeepsLegacyNoOpSuccessSurface() {
        ReportController controller = new ReportController();
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
        when(dataQueryService.queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_state`='0'")))
                .thenReturn(new ListViewResult());

        ReportController controller = new ReportController();
        setField(controller, "dataQueryService", dataQueryService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setCurrentPage(1);
        request.setPageSize(10);
        request.setFilterExp(filterExp("order_state", "1", "等于", "0", "Open"));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        verify(dataQueryService).queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_state`='0'"));
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsLegacyContainsFilterExpToQueryFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(property("symbol", "order_symbol")));
        when(dataQueryService.queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_symbol` LIKE '%BTC%'")))
                .thenReturn(new ListViewResult());

        ReportController controller = new ReportController();
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(filterExp("symbol", "7", "包含", "BTC", "BTC"));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        verify(dataQueryService).queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_symbol` LIKE '%BTC%'"));
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsLegacyFilterExpColumnIdToModelColumn() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(property(11L, "price", "order_price")));
        when(dataQueryService.queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_price`>'100'")))
                .thenReturn(new ListViewResult());

        ReportController controller = new ReportController();
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(filterExpById("11", "3", "大于", "100", "100"));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        verify(dataQueryService).queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class), eq("`order_price`>'100'"));
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportMapsLegacyCompositeFilterExpToQueryFilter() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DaoService daoService = mock(DaoService.class);
        when(daoService.getOneDetailByKey(eq(View.class), eq("100"))).thenReturn(view("100"));
        when(daoService.getOneDetailByKey(eq(Model.class), eq("100"))).thenReturn(model(property("symbol", "order_symbol")));
        when(dataQueryService.queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class),
                eq("(`order_state`='0') And (`order_symbol` LIKE '%BTC%') OR (`order_price`>'100')")))
                .thenReturn(new ListViewResult());

        ReportController controller = new ReportController();
        setField(controller, "dataQueryService", dataQueryService);
        setField(controller, "daoService", daoService);

        MakeReportRequest request = new MakeReportRequest();
        request.setViewId(100L);
        request.setFilterExp(compositeFilterExp(
                filterExp("order_state", "1", "等于", "0", "Open"),
                seq("and", "与", filterExp("symbol", "7", "包含", "BTC", "BTC")),
                seq("or", "或", filterExp("order_price", "3", "大于", "100", "100"))));

        CommonResponse<ReportGridResult> response = controller.makeReport(request);

        verify(dataQueryService).queryLegacyViewData(eq("100"), org.mockito.ArgumentMatchers.any(PageNavigator.class),
                eq("(`order_state`='0') And (`order_symbol` LIKE '%BTC%') OR (`order_price`>'100')"));
        assertEquals(0, response.getCode());
    }

    @Test
    public void makeReportRejectsUnknownFilterExpInsteadOfIgnoringIt() throws Exception {
        ReportController controller = new ReportController();
        MakeReportRequest request = new MakeReportRequest();
        request.setFilterExp(filterExp("order_state", "99", "自定义", "0", "Open"));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(request));
    }

    @Test
    public void makeReportRejectsUnknownCompositeBoolOp() throws Exception {
        ReportController controller = new ReportController();
        MakeReportRequest request = new MakeReportRequest();
        request.setFilterExp(compositeFilterExp(
                filterExp("order_state", "1", "等于", "0", "Open"),
                seq("or 1=1", "or 1=1", filterExp("symbol", "7", "包含", "BTC", "BTC"))));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(request));

        MakeReportRequest emptyBoolOpRequest = new MakeReportRequest();
        emptyBoolOpRequest.setFilterExp(compositeFilterExp(
                filterExp("order_state", "1", "等于", "0", "Open"),
                seq(null, null, filterExp("symbol", "7", "包含", "BTC", "BTC"))));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(emptyBoolOpRequest));
    }

    private static ListDataItem row() {
        ListDataItem row = new ListDataItem();
        row.setValues(Map.of("symbol", "BTC-USDT", "state", "Open"));
        row.setItems(List.of(value("symbol", "Symbol", "BTC-USDT"), value("state", "State", "Open")));
        return row;
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
