package org.fool.framework.view.api;

import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.report.ReportCell;
import org.fool.framework.report.ReportGridResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.MakeReportRequest;
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
    public void makeReportRejectsUnsupportedFilterExpInsteadOfIgnoringIt() throws Exception {
        ReportController controller = new ReportController();
        MakeReportRequest request = new MakeReportRequest();
        request.setFilterExp(filterExp("order_state", "7", "包含", "0", "Open"));

        assertThrows(IllegalArgumentException.class, () -> controller.makeReport(request));
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
