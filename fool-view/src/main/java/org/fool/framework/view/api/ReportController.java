package org.fool.framework.view.api;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.report.ReportGridRenderer;
import org.fool.framework.report.ReportGridResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.MakeReportRequest;
import org.fool.framework.view.model.View;
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
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {
    @Autowired
    private DataQueryService dataQueryService;
    @Autowired
    private DaoService daoService;

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
        return simpleFilterExp(request, request.getFilterExp());
    }

    private String simpleFilterExp(MakeReportRequest request, MakeReportRequest.BoolExp filterExp) {
        if (filterExp == null) {
            return null;
        }
        if (filterExp.getFirstExp() != null || !CollectionUtils.isEmpty(filterExp.getSequences())) {
            throw new IllegalArgumentException("Only simple FilterExp compare types are supported.");
        }
        String column = filterColumn(request, filterExp);
        String op = compareSql(filterExp.getCompareOp());
        if (!safeColumn(column) || op == null) {
            throw new IllegalArgumentException("Only simple FilterExp compare types are supported.");
        }
        String value = filterExp.getValueExp() == null ? "" : filterExp.getValueExp();
        return "`" + column.trim() + "`" + op + "'" + filterValue(op, value) + "'";
    }

    private String filterColumn(MakeReportRequest request, MakeReportRequest.BoolExp filterExp) {
        String column = filterExp.getCol() == null ? null : filterExp.getCol().getName();
        if (!StringUtils.hasText(column) && filterExp.getCol() != null) {
            column = filterExp.getCol().getId();
        }
        String mappedColumn = mappedColumn(request, column);
        return StringUtils.hasText(mappedColumn) ? mappedColumn : column;
    }

    private String mappedColumn(MakeReportRequest request, String column) {
        if (daoService == null || request.getViewId() == null || !safePropertyToken(column)) {
            return null;
        }
        View view = daoService.getOneDetailByKey(View.class, request.getViewId().toString());
        if (view == null || !StringUtils.hasText(view.getViewModel())) {
            return null;
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null || CollectionUtils.isEmpty(model.getProperties())) {
            return null;
        }
        String token = column.trim();
        return model.getProperties().stream()
                .filter(property -> matchesProperty(property, token))
                .map(Property::getColumn)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private boolean matchesProperty(Property property, String token) {
        return Objects.equals(property.getName(), token)
                || Objects.equals(property.getColumn(), token)
                || (property.getId() != null && Objects.equals(property.getId().toString(), token));
    }

    private boolean safeColumn(String column) {
        return StringUtils.hasText(column) && column.trim().matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private boolean safePropertyToken(String token) {
        return safeColumn(token) || (StringUtils.hasText(token) && token.trim().matches("\\d+"));
    }

    private String compareSql(MakeReportRequest.CompareOpItem compareOp) {
        if (compareOp == null) {
            return null;
        }
        switch (compareOp.getId() == null ? "" : compareOp.getId().trim()) {
            case "1":
                return "=";
            case "2":
                return "<>";
            case "3":
                return ">";
            case "4":
                return ">=";
            case "5":
                return "<";
            case "6":
                return "<=";
            case "7":
                return " LIKE ";
            default:
                break;
        }
        String name = compareOp.getName() == null ? "" : compareOp.getName().trim().toLowerCase(Locale.ROOT);
        switch (name) {
            case "=":
            case "==":
            case "等于":
            case "equal":
                return "=";
            case "!=":
            case "<>":
            case "不等于":
                return "<>";
            case ">":
            case "大于":
                return ">";
            case ">=":
            case "大于等于":
                return ">=";
            case "<":
            case "小于":
                return "<";
            case "<=":
            case "小于等于":
                return "<=";
            case "包含":
            case "like":
            case "contains":
                return " LIKE ";
            default:
                return null;
        }
    }

    private String filterValue(String op, String value) {
        String escaped = value.replace("'", "''");
        return " LIKE ".equals(op) ? "%" + escaped + "%" : escaped;
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
