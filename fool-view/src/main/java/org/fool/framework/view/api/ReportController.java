package org.fool.framework.view.api;

import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.report.ReportGridRenderer;
import org.fool.framework.report.ReportGridResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.MakeReportRequest;
import org.fool.framework.view.service.DataQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {
    @Autowired
    private DataQueryService dataQueryService;

    private final ReportGridRenderer renderer = new ReportGridRenderer();

    @PostMapping("/makereport")
    @ResponseBody
    public CommonResponse<ReportGridResult> makeReport(@RequestBody MakeReportRequest request) {
        PageNavigator page = new PageNavigator();
        page.setPageIndex(request.getCurrentPage() == null ? 0 : request.getCurrentPage());
        page.setPageSize(request.getPageSize() == null ? 0 : request.getPageSize());
        ListViewResult queryResult = dataQueryService.queryLegacyViewData(
                request.getViewId() == null ? null : request.getViewId().toString(),
                page,
                queryFilter(request));
        return new CommonResponse<>(renderer.render(
                request.getViewId() == null ? 0 : request.getViewId().intValue(),
                page.getPageIndex(),
                page.getPageSize(),
                queryResult.getTotalItem() == null ? 0 : queryResult.getTotalItem(),
                queryResult.getTotalPage() == null ? 0 : queryResult.getTotalPage(),
                columns(request, queryResult),
                rows(queryResult)));
    }

    private String queryFilter(MakeReportRequest request) {
        if (StringUtils.hasText(request.getQueryFilter())) {
            return request.getQueryFilter();
        }
        return simpleFilterExp(request.getFilterExp());
    }

    private String simpleFilterExp(MakeReportRequest.BoolExp filterExp) {
        if (filterExp == null) {
            return null;
        }
        if (filterExp.getFirstExp() != null || !CollectionUtils.isEmpty(filterExp.getSequences())) {
            throw new IllegalArgumentException("Only simple equality FilterExp is supported.");
        }
        String column = filterExp.getCol() == null ? null : filterExp.getCol().getName();
        if (!StringUtils.hasText(column) && filterExp.getCol() != null) {
            column = filterExp.getCol().getId();
        }
        if (!safeColumn(column) || !equalCompare(filterExp.getCompareOp())) {
            throw new IllegalArgumentException("Only simple equality FilterExp is supported.");
        }
        String value = filterExp.getValueExp() == null ? "" : filterExp.getValueExp();
        return "`" + column.trim() + "`='" + value.replace("'", "''") + "'";
    }

    private boolean safeColumn(String column) {
        return StringUtils.hasText(column) && column.trim().matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private boolean equalCompare(MakeReportRequest.CompareOpItem compareOp) {
        if (compareOp == null) {
            return false;
        }
        if ("1".equals(compareOp.getId())) {
            return true;
        }
        String name = compareOp.getName() == null ? "" : compareOp.getName().trim().toLowerCase(Locale.ROOT);
        return "=".equals(name) || "==".equals(name) || "等于".equals(name) || "equal".equals(name);
    }

    private List<String> columns(MakeReportRequest request, ListViewResult queryResult) {
        if (!CollectionUtils.isEmpty(request.getReportCols())) {
            return request.getReportCols().stream()
                    .sorted(Comparator.comparingInt(col -> col.getIndex() == null ? 0 : col.getIndex()))
                    .map(MakeReportRequest.ReportCol::getColName)
                    .filter(StringUtils::hasText)
                    .toList();
        }
        return queryResult.getCols() == null ? List.of() : queryResult.getCols();
    }

    private List<Map<String, Object>> rows(ListViewResult queryResult) {
        List<ListDataItem> items = queryResult.getData() == null ? queryResult.getItems() : queryResult.getData();
        if (items == null) {
            return List.of();
        }
        return items.stream().map(this::row).toList();
    }

    private Map<String, Object> row(ListDataItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (item.getValues() != null) {
            row.putAll(item.getValues());
        }
        if (item.getItems() != null) {
            for (ListDataValue value : item.getItems()) {
                if (StringUtils.hasText(value.getPrpId())) {
                    row.put(value.getPrpId(), value.getFmtValue());
                }
                if (StringUtils.hasText(value.getPrpShowName())) {
                    row.put(value.getPrpShowName(), value.getFmtValue());
                }
            }
        }
        return row;
    }
}
