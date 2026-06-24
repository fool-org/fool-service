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

        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
            result.getCells().add(cell(columnIndex, 0, columns.get(columnIndex)));
        }

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Object> row = rows.get(rowIndex);
            for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
                String column = columns.get(columnIndex);
                result.getCells().add(cell(columnIndex, rowIndex + 1, Objects.toString(row.get(column), "")));
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
