package org.fool.framework.report;

import org.fool.framework.common.PropertyType;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReportMigrationTest {
    @Test
    public void preservesLegacyReportEnumOrdinals() {
        assertEquals(List.of("ASC", "DESC"),
                Arrays.stream(OrderType.values()).map(Enum::name).toList());
        assertEquals(List.of("Max", "Min", "Avg", "Sum", "Ignore"),
                Arrays.stream(StaticType.values()).map(Enum::name).toList());
        assertEquals(List.of("Null", "Row", "Column"),
                Arrays.stream(CalDirection.values()).map(Enum::name).toList());
    }

    @Test
    public void constructorsKeepLegacyDefaults() {
        CellFormat cellFormat = new CellFormat();
        assertTrue(cellFormat.getStaticFormats().isEmpty());

        TableFormat tableFormat = new TableFormat();
        assertTrue(tableFormat.getColums().isEmpty());
        assertTrue(tableFormat.getRows().isEmpty());
        assertTrue(tableFormat.getValueCell().isEmpty());

        SingleCell singleCell = new SingleCell();
        singleCell.setValue("north");
        assertEquals(1, singleCell.getSpan());
        assertEquals(1, singleCell.getOtherSpan());
        assertFalse(singleCell.isMegerToParent());
        assertEquals("north 1", singleCell.toString());

        MatrixTable table = new MatrixTable();
        assertTrue(table.getColHeaders().isEmpty());
        assertTrue(table.getRowHeaders().isEmpty());
        assertTrue(table.getCells().isEmpty());

        assertSame(ReportEmptyValue.getValue(), ReportEmptyValue.getValue());
    }

    @Test
    public void tableHeaderKeepsLegacyUnsupportedGetterNoOpSetterSurface() {
        TableHeader header = new TableHeader();

        header.setSourceDataCol(1);
        header.setIndex(2);
        header.setAddSum(3);

        assertThrows(UnsupportedOperationException.class, header::getSourceDataCol);
        assertThrows(UnsupportedOperationException.class, header::getIndex);
        assertThrows(UnsupportedOperationException.class, header::getAddSum);
    }

    @Test
    public void matrixResultKeepsLegacyUnsupportedAddSurface() {
        MatrixResult result = new MatrixResult();

        assertThrows(UnsupportedOperationException.class, result::add);
    }

    @Test
    public void reportFactoryPreservesLegacyEmptyShell() {
        Class<?> factory = loadClass("org.fool.framework.report.ReportFactory");

        assertEquals(0, factory.getDeclaredFields().length);
        assertEquals(0, factory.getDeclaredMethods().length);
    }

    @Test
    public void getCellsMapsMatrixHeadersAndDataRectsToAbsoluteCoordinates() {
        MatrixTable table = new MatrixTable();
        table.getColHeaders().add(List.of(singleCell("2026", 2, false)));
        table.getRowHeaders().add(List.of(singleCell("north", 1, false), singleCell("south", 1, false)));

        DataRect north = new DataRect();
        north.setColHeaderIndex(0);
        north.setRowHeaderIndex(0);
        north.getCells().add(valueCell(10));
        table.getCells().add(north);

        DataRect south = new DataRect();
        south.setColHeaderIndex(1);
        south.setRowHeaderIndex(1);
        south.getCells().add(valueCell(20));
        table.getCells().add(south);

        List<Cell> cells = new MatrixTableFactory().getCells(table);

        assertEquals(5, cells.size());
        assertCell(cells.get(0), 1, 0, 2, 1, "2026");
        assertCell(cells.get(1), 0, 1, 1, 1, "north");
        assertCell(cells.get(2), 0, 2, 1, 1, "south");
        assertCell(cells.get(3), 1, 1, 1, 1, 10);
        assertCell(cells.get(4), 2, 2, 1, 1, 20);
    }

    @Test
    public void calculatedCellsShiftLegacyScopeByRenderedOffset() {
        MatrixTable table = new MatrixTable();
        table.getColHeaders().add(List.of(singleCell("total", 1, false)));
        table.getRowHeaders().add(List.of(singleCell("north", 1, false)));

        DataRect rect = new DataRect();
        rect.setColHeaderIndex(0);
        rect.setRowHeaderIndex(0);
        Cell calculated = valueCell("sum");
        calculated.setCalculate(true);
        calculated.setCalDirection(CalDirection.Column);
        calculated.setCalScope("0-1,3-4");
        calculated.setExpression(StaticType.Sum);
        rect.getCells().add(calculated);
        table.getCells().add(rect);

        Cell rendered = new MatrixTableFactory().getCells(table).get(2);

        assertTrue(rendered.isCalculate());
        assertEquals(CalDirection.Column, rendered.getCalDirection());
        assertEquals(StaticType.Sum, rendered.getExpression());
        assertEquals("1-2,4-5", rendered.getCalScope());
    }

    @Test
    public void cellFactoryOrdersCellsByColumnThenRowInPlace() {
        List<Cell> cells = new java.util.ArrayList<>(List.of(
                positionedCell(2, 1, "c"),
                positionedCell(1, 3, "b"),
                positionedCell(1, 2, "a")));

        new CellFactory().ordCells(cells);

        assertEquals(List.of("a", "b", "c"),
                cells.stream().map(Cell::getValue).toList());
    }

    @Test
    public void reportStoresLegacySupportedDefinitionShape() {
        Param param = new Param();
        param.setName("region");
        param.setFormat("string");

        ParamInput input = new ParamInput();
        input.setParam(param);
        input.setValue("north");
        input.setShow("North");

        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setName("Sales Report");
        report.setId(reportId);
        report.setNo("RPT-001");
        report.setParams(List.of(param));

        assertEquals("Sales Report", report.getName());
        assertEquals(reportId, report.getId());
        assertEquals("RPT-001", report.getNo());
        assertEquals("region", report.getParams().get(0).getName());
        assertEquals("North", input.getShow());
    }

    @Test
    public void reportKeepsLegacyUnsupportedGetterNoOpSetterSurface() {
        Report report = new Report();

        report.setResult(List.of(new ReportResultTable()));
        report.setSource(new IReportSource() {
        });
        report.setCreateTime(LocalDateTime.of(2026, 6, 24, 9, 10));
        report.setCreatePerson("admin");
        report.setModifyTime(LocalDateTime.of(2026, 6, 24, 9, 20));
        report.setMoidiyPerson("operator");

        assertThrows(UnsupportedOperationException.class, report::getResult);
        assertThrows(UnsupportedOperationException.class, report::getSource);
        assertThrows(UnsupportedOperationException.class, report::getCreateTime);
        assertThrows(UnsupportedOperationException.class, report::getCreatePerson);
        assertThrows(UnsupportedOperationException.class, report::getModifyTime);
        assertThrows(UnsupportedOperationException.class, report::getMoidiyPerson);
    }

    @Test
    public void reportResultKeepsLegacyUnsupportedGetterNoOpSetterSurface() {
        ReportResult result = new ReportResult();

        result.setReport(new Report());
        result.setResult(List.of());
        result.setReportTime("2026-06-23 09:50");
        result.setReportPerson("admin");
        result.setInputs(List.of(new ParamInput()));
        result.setTitle("Sales Report");

        assertThrows(UnsupportedOperationException.class, result::getReport);
        assertThrows(UnsupportedOperationException.class, result::getResult);
        assertThrows(UnsupportedOperationException.class, result::getReportTime);
        assertThrows(UnsupportedOperationException.class, result::getReportPerson);
        assertThrows(UnsupportedOperationException.class, result::getInputs);
        assertThrows(UnsupportedOperationException.class, result::getTitle);
    }

    @Test
    public void reportResultTableKeepsLegacyUnsupportedGetterNoOpSetterSurface() {
        ReportResultTable table = new ReportResultTable();
        ReportResultTableColumn column = new ReportResultTableColumn();

        table.setName("Sales");
        table.setColumns(List.of(column));
        column.setColName("Amount");
        column.setDataType(PropertyType.Decimal);
        column.setIndex(2);

        assertThrows(UnsupportedOperationException.class, table::getName);
        assertThrows(UnsupportedOperationException.class, table::getColumns);
        assertThrows(UnsupportedOperationException.class, column::getColName);
        assertThrows(UnsupportedOperationException.class, column::getDataType);
        assertThrows(UnsupportedOperationException.class, column::getIndex);
    }

    @Test
    public void createMatrixTableBuildsHeadersAndDataRectsFromSourceRows() {
        TableFormat format = new TableFormat();
        format.getColums().add(formatCell("year"));
        format.getRows().add(formatCell("region"));
        ValueCell amount = new ValueCell();
        amount.setSourceColumn("amount");
        format.getValueCell().add(amount);

        MatrixTable table = new MatrixTableFactory().createMatrixTable(
                format,
                List.of(
                        row("year", 2025, "region", "north", "amount", 10),
                        row("year", 2026, "region", "south", "amount", 20)));

        assertEquals(List.of("2025", "2026"),
                table.getColHeaders().get(0).stream().map(SingleCell::getValue).toList());
        assertEquals(List.of("north", "south"),
                table.getRowHeaders().get(0).stream().map(SingleCell::getValue).toList());
        assertEquals(2, table.getCells().size());
        assertEquals(0, table.getCells().get(0).getColHeaderIndex());
        assertEquals(0, table.getCells().get(0).getRowHeaderIndex());
        assertEquals(10, table.getCells().get(0).getCells().get(0).getValue());
        assertEquals(1, table.getCells().get(1).getColHeaderIndex());
        assertEquals(1, table.getCells().get(1).getRowHeaderIndex());
        assertEquals(20, table.getCells().get(1).getCells().get(0).getValue());
    }

    @Test
    public void createMatrixTableAddsLegacyRowStaticSubtotalCells() {
        TableFormat format = new TableFormat();
        format.getColums().add(formatCell("year"));
        CellFormat region = formatCell("region");
        region.getStaticFormats().add(staticFormat("Total", StaticType.Sum));
        format.getRows().add(region);
        ValueCell amount = new ValueCell();
        amount.setSourceColumn("amount");
        format.getValueCell().add(amount);

        MatrixTable table = new MatrixTableFactory().createMatrixTable(
                format,
                List.of(
                        row("year", 2026, "region", "north", "amount", 10),
                        row("year", 2026, "region", "south", "amount", 20)));

        assertEquals(List.of("north", "south", "Total"),
                table.getRowHeaders().get(0).stream().map(SingleCell::getValue).toList());
        assertEquals(3, table.getCells().size());

        DataRect subtotal = table.getCells().get(2);
        assertEquals(0, subtotal.getColHeaderIndex());
        assertEquals(2, subtotal.getRowHeaderIndex());
        Cell subtotalCell = subtotal.getCells().get(0);
        assertTrue(subtotalCell.isCalculate());
        assertEquals(CalDirection.Column, subtotalCell.getCalDirection());
        assertEquals("0-1", subtotalCell.getCalScope());
        assertEquals(StaticType.Sum, subtotalCell.getExpression());
        assertEquals("Total0", subtotalCell.getValue());
    }

    @Test
    public void createMatrixTableScopesNestedRowStaticSubtotalsToSiblingLeaves() {
        TableFormat format = new TableFormat();
        format.getColums().add(formatCell("year"));
        format.getRows().add(formatCell("region"));
        CellFormat city = formatCell("city");
        city.getStaticFormats().add(staticFormat("Subtotal", StaticType.Sum));
        format.getRows().add(city);
        ValueCell amount = new ValueCell();
        amount.setSourceColumn("amount");
        format.getValueCell().add(amount);

        MatrixTable table = new MatrixTableFactory().createMatrixTable(
                format,
                List.of(
                        row("year", 2026, "region", "north", "city", "A", "amount", 10),
                        row("year", 2026, "region", "north", "city", "B", "amount", 20),
                        row("year", 2026, "region", "south", "city", "C", "amount", 30)));

        assertEquals(List.of("north", "south"),
                table.getRowHeaders().get(0).stream().map(SingleCell::getValue).toList());
        assertEquals(List.of("A", "B", "Subtotal", "C", "Subtotal"),
                table.getRowHeaders().get(1).stream().map(SingleCell::getValue).toList());

        DataRect northSubtotal = table.getCells().get(3);
        assertEquals(2, northSubtotal.getRowHeaderIndex());
        assertEquals("0-1", northSubtotal.getCells().get(0).getCalScope());
        assertEquals("Subtotal0", northSubtotal.getCells().get(0).getValue());

        DataRect southSubtotal = table.getCells().get(4);
        assertEquals(4, southSubtotal.getRowHeaderIndex());
        assertEquals("3-3", southSubtotal.getCells().get(0).getCalScope());
        assertEquals("Subtotal0", southSubtotal.getCells().get(0).getValue());
    }

    @Test
    public void createMatrixTableAddsLegacyColumnStaticSubtotalCells() {
        TableFormat format = new TableFormat();
        CellFormat year = formatCell("year");
        year.getStaticFormats().add(staticFormat("Total", StaticType.Sum));
        format.getColums().add(year);
        format.getRows().add(formatCell("region"));
        ValueCell amount = new ValueCell();
        amount.setSourceColumn("amount");
        format.getValueCell().add(amount);

        MatrixTable table = new MatrixTableFactory().createMatrixTable(
                format,
                List.of(
                        row("year", 2025, "region", "north", "amount", 10),
                        row("year", 2026, "region", "north", "amount", 20)));

        assertEquals(List.of("2025", "2026", "Total"),
                table.getColHeaders().get(0).stream().map(SingleCell::getValue).toList());
        assertEquals(3, table.getCells().size());

        DataRect subtotal = table.getCells().get(2);
        assertEquals(2, subtotal.getColHeaderIndex());
        assertEquals(0, subtotal.getRowHeaderIndex());
        Cell subtotalCell = subtotal.getCells().get(0);
        assertTrue(subtotalCell.isCalculate());
        assertEquals(CalDirection.Row, subtotalCell.getCalDirection());
        assertEquals("0-1", subtotalCell.getCalScope());
        assertEquals(StaticType.Sum, subtotalCell.getExpression());
        assertEquals("Total0", subtotalCell.getValue());
    }

    @Test
    public void createMatrixTableScopesNestedColumnStaticSubtotalsToSiblingLeaves() {
        TableFormat format = new TableFormat();
        format.getColums().add(formatCell("year"));
        CellFormat quarter = formatCell("quarter");
        quarter.getStaticFormats().add(staticFormat("Subtotal", StaticType.Sum));
        format.getColums().add(quarter);
        format.getRows().add(formatCell("region"));
        ValueCell amount = new ValueCell();
        amount.setSourceColumn("amount");
        format.getValueCell().add(amount);

        MatrixTable table = new MatrixTableFactory().createMatrixTable(
                format,
                List.of(
                        row("year", 2026, "quarter", "Q1", "region", "north", "amount", 10),
                        row("year", 2026, "quarter", "Q2", "region", "north", "amount", 20),
                        row("year", 2027, "quarter", "Q1", "region", "north", "amount", 30)));

        assertEquals(List.of("2026", "2027"),
                table.getColHeaders().get(0).stream().map(SingleCell::getValue).toList());
        assertEquals(List.of("Q1", "Q2", "Subtotal", "Q1", "Subtotal"),
                table.getColHeaders().get(1).stream().map(SingleCell::getValue).toList());

        DataRect subtotal2026 = table.getCells().get(3);
        assertEquals(2, subtotal2026.getColHeaderIndex());
        assertEquals("0-1", subtotal2026.getCells().get(0).getCalScope());
        assertEquals("Subtotal0", subtotal2026.getCells().get(0).getValue());

        DataRect subtotal2027 = table.getCells().get(4);
        assertEquals(4, subtotal2027.getColHeaderIndex());
        assertEquals("3-3", subtotal2027.getCells().get(0).getCalScope());
        assertEquals("Subtotal0", subtotal2027.getCells().get(0).getValue());
    }

    @Test
    public void createMatrixTableScopesDeepColumnStaticSubtotalsToSharedAncestors() {
        TableFormat format = new TableFormat();
        format.getColums().add(formatCell("year"));
        format.getColums().add(formatCell("quarter"));
        CellFormat month = formatCell("month");
        month.getStaticFormats().add(staticFormat("Subtotal", StaticType.Sum));
        format.getColums().add(month);
        format.getRows().add(formatCell("region"));
        ValueCell amount = new ValueCell();
        amount.setSourceColumn("amount");
        format.getValueCell().add(amount);

        MatrixTable table = new MatrixTableFactory().createMatrixTable(
                format,
                List.of(
                        row("year", 2026, "quarter", "Q1", "month", "Jan", "region", "north", "amount", 10),
                        row("year", 2026, "quarter", "Q1", "month", "Feb", "region", "north", "amount", 20),
                        row("year", 2026, "quarter", "Q2", "month", "Mar", "region", "north", "amount", 30),
                        row("year", 2027, "quarter", "Q1", "month", "Jan", "region", "north", "amount", 40)));

        assertEquals(List.of("Jan", "Feb", "Subtotal", "Mar", "Subtotal", "Jan", "Subtotal"),
                table.getColHeaders().get(2).stream().map(SingleCell::getValue).toList());

        DataRect q2Subtotal = table.getCells().get(5);
        assertEquals(4, q2Subtotal.getColHeaderIndex());
        assertEquals("0-1,3-3", q2Subtotal.getCells().get(0).getCalScope());
        assertEquals("Subtotal0", q2Subtotal.getCells().get(0).getValue());
    }

    @Test
    public void reportGridRendererBuildsLegacyMakeReportCellsFromFlatRows() {
        ReportGridResult result = new ReportGridRenderer().render(
                12,
                2,
                20,
                42,
                3,
                List.of("name", "amount"),
                List.of(
                        row("name", "north", "amount", 10),
                        row("name", "south", "amount", 20)));

        assertEquals(12, result.getViewId());
        assertEquals(2, result.getCurrentPage());
        assertEquals(20, result.getPageSize());
        assertEquals(42, result.getTotalRecords());
        assertEquals(3, result.getTotalPages());
        assertEquals(6, result.getCells().size());
        assertReportCell(result.getCells().get(0), 0, 0, "name");
        assertReportCell(result.getCells().get(1), 0, 0, "amount");
        assertReportCell(result.getCells().get(2), 0, 1, "north");
        assertReportCell(result.getCells().get(3), 0, 1, "10");
        assertReportCell(result.getCells().get(4), 0, 2, "south");
        assertReportCell(result.getCells().get(5), 0, 2, "20");
    }

    @Test
    public void reportGridRendererKeepsHeadersAndPagingForEmptyRows() {
        ReportGridResult result = new ReportGridRenderer().render(
                7,
                1,
                50,
                0,
                0,
                List.of("name", "amount"),
                List.of());

        assertEquals(7, result.getViewId());
        assertEquals(1, result.getCurrentPage());
        assertEquals(50, result.getPageSize());
        assertEquals(0, result.getTotalRecords());
        assertEquals(0, result.getTotalPages());
        assertEquals(2, result.getCells().size());
        assertReportCell(result.getCells().get(0), 0, 0, "name");
        assertReportCell(result.getCells().get(1), 0, 0, "amount");
    }

    private static SingleCell singleCell(String value, int span, boolean mergeToParent) {
        SingleCell cell = new SingleCell();
        cell.setValue(value);
        cell.setSpan(span);
        cell.setMegerToParent(mergeToParent);
        return cell;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            fail(className + " should exist");
            return null;
        }
    }

    private static Cell valueCell(Object value) {
        Cell cell = new Cell();
        cell.setValue(value);
        return cell;
    }

    private static Cell positionedCell(int column, int row, Object value) {
        Cell cell = valueCell(value);
        cell.setColumn(column);
        cell.setRow(row);
        return cell;
    }

    private static CellFormat formatCell(String sourceColumn) {
        CellFormat format = new CellFormat();
        format.setSourceColumn(sourceColumn);
        return format;
    }

    private static StaticFormat staticFormat(String name, StaticType type) {
        StaticCellFormate cell = new StaticCellFormate();
        cell.setStaticType(type);
        StaticFormat format = new StaticFormat();
        format.setName(name);
        format.getStaticsCells().add(cell);
        return format;
    }

    private static Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            row.put((String) values[i], values[i + 1]);
        }
        return row;
    }

    private static void assertCell(
            Cell cell,
            int column,
            int row,
            int colSpan,
            int rowSpan,
            Object value) {
        assertEquals(column, cell.getColumn());
        assertEquals(row, cell.getRow());
        assertEquals(colSpan, cell.getColSpan());
        assertEquals(rowSpan, cell.getRowSpan());
        assertEquals(value, cell.getValue());
    }

    private static void assertReportCell(ReportCell cell, int col, int row, String value) {
        assertEquals(col, cell.getCol());
        assertEquals(row, cell.getRow());
        assertEquals(1, cell.getColSpan());
        assertEquals(1, cell.getRowSpan());
        assertEquals(value, cell.getFmtValue());
    }
}
