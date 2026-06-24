package org.fool.framework.report;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReportGridRenderer {
    public ReportGridResult render(
            int viewId,
            int currentPage,
            int pageSize,
            long totalRecords,
            long totalPages,
            List<String> columns,
            List<Map<String, Object>> rows) {
        ReportGridResult result = new ReportGridResult();
        result.setViewId(viewId);
        result.setCurrentPage(currentPage);
        result.setPageSize(pageSize);
        result.setTotalRecords(totalRecords);
        result.setTotalPages(totalPages);

        TableFormat format = new TableFormat();
        for (String column : columns) {
            ValueCell valueCell = new ValueCell();
            valueCell.setSourceColumn(column);
            valueCell.setName(column);
            format.getValueCell().add(valueCell);
            result.getCells().add(cell(0, 0, column));
        }

        MatrixTable matrix = new MatrixTableFactory().createMatrixTable(format, rows);
        int rowIndex = 0;
        for (DataRect dataRect : matrix.getCells()) {
            rowIndex++;
            for (Cell valueCell : dataRect.getCells()) {
                result.getCells().add(cell(0, rowIndex, Objects.toString(valueCell.getValue(), "")));
            }
        }

        return result;
    }

    private static ReportCell cell(int col, int row, String value) {
        ReportCell cell = new ReportCell();
        cell.setCol(col);
        cell.setRow(row);
        cell.setColSpan(1);
        cell.setRowSpan(1);
        cell.setFmtValue(value);
        return cell;
    }
}
