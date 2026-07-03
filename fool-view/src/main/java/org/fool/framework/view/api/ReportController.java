package org.fool.framework.view.api;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.common.PropertyType;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.JdbcCompareOpCatalog;
import org.fool.framework.query.JdbcSelectTypeCatalog;
import org.fool.framework.report.ReportGridRenderer;
import org.fool.framework.report.ReportGridResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListDataValue;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.MakeReportRequest;
import org.fool.framework.view.dto.ReportModelResult;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
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
    @Autowired(required = false)
    private JdbcCompareOpCatalog compareOpCatalog;
    @Autowired(required = false)
    private JdbcSelectTypeCatalog selectTypeCatalog;

    private final ReportGridRenderer renderer = new ReportGridRenderer();

    @PostMapping({"/getmkqview", "/mkqview"})
    @ResponseBody
    public CommonResponse<ReportModelResult> getReportModel(@RequestBody MakeReportRequest request) {
        ReportModelResult result = new ReportModelResult();
        View view = request.getViewId() == null ? null : daoService.getOneDetailByKey(View.class, request.getViewId().toString());
        if (view == null || !StringUtils.hasText(view.getViewModel())) {
            return new CommonResponse<>(result);
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null || CollectionUtils.isEmpty(model.getProperties())) {
            return new CommonResponse<>(result);
        }
        result.setCols(reportModelCols(view, model));
        return new CommonResponse<>(result);
    }

    @PostMapping({"/makereport", "/getrpt"})
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
                rows(request, queryResult)));
    }

    @PostMapping("/saverpt")
    @ResponseBody
    public CommonResponse<Void> saveReport(@RequestBody MakeReportRequest request) {
        return new CommonResponse<>((Void) null);
    }

    private List<ReportModelResult.QueryCol> reportModelCols(View view, Model model) {
        if (!CollectionUtils.isEmpty(view.getListItems())) {
            return view.getListItems().stream()
                    .sorted(Comparator.comparingInt(this::safeShowIndex))
                    .map(item -> reportModelCol(propertyForItem(model, item), item.getItemName()))
                    .filter(Objects::nonNull)
                    .toList();
        }
        return model.getProperties().stream()
                .map(property -> reportModelCol(property, null))
                .filter(Objects::nonNull)
                .toList();
    }

    private int safeShowIndex(ViewItem item) {
        return item.getShowIndex() == null ? 0 : item.getShowIndex();
    }

    private Property propertyForItem(Model model, ViewItem item) {
        if (item.getProperty() != null) {
            return item.getProperty();
        }
        if (!StringUtils.hasText(item.getModelProperty()) || CollectionUtils.isEmpty(model.getProperties())) {
            return null;
        }
        return model.getProperties().stream()
                .filter(property -> Objects.equals(property.getName(), item.getModelProperty()))
                .findFirst()
                .orElse(null);
    }

    private ReportModelResult.QueryCol reportModelCol(Property property, String name) {
        if (property == null || Boolean.TRUE.equals(property.getIsCollection()) || property.getPropertyType() == null) {
            return null;
        }
        ReportModelResult.QueryCol col = new ReportModelResult.QueryCol();
        PropertyType type = property.getPropertyType();
        col.setId(property.getId() == null ? property.getName() : property.getId().toString());
        col.setName(StringUtils.hasText(name) ? name : propertyShowName(property));
        col.setPrpType(type.code());
        col.setModelId(property.getPropertyModel() == null ? null : property.getPropertyModel().getId());
        col.setStates(states(property.getPropertyModel()));
        col.setCompareTypes(compareOpCatalog == null ? List.of() : compareOpCatalog.listFor(type).stream()
                .map(op -> new ReportModelResult.Option(op.getId() + "", op.getShowName()))
                .toList());
        col.setQueryTypes(selectTypeCatalog == null ? List.of() : selectTypeCatalog.listFor(type).stream()
                .map(typeItem -> new ReportModelResult.Option(typeItem.getId() + "", typeItem.getShow()))
                .toList());
        return col;
    }

    private String propertyShowName(Property property) {
        return StringUtils.hasText(property.getRemark()) ? property.getRemark() : property.getName();
    }

    private List<ReportModelResult.StateValue> states(Model model) {
        if (model == null || CollectionUtils.isEmpty(model.getEnumValues())) {
            return List.of();
        }
        return model.getEnumValues().stream()
                .map(this::stateValue)
                .toList();
    }

    private ReportModelResult.StateValue stateValue(EnumValue enumValue) {
        ReportModelResult.StateValue state = new ReportModelResult.StateValue();
        state.setShowName(enumValue.getName());
        state.setDbName(enumValue.getValue());
        return state;
    }

    private String queryFilter(MakeReportRequest request) {
        if (StringUtils.hasText(request.getQueryFilter())) {
            return request.getQueryFilter();
        }
        return filterExpSql(request, request.getFilterExp());
    }

    private String filterExpSql(MakeReportRequest request, MakeReportRequest.BoolExp filterExp) {
        if (filterExp == null) {
            return null;
        }
        if (filterExp.getFirstExp() != null) {
            String result = "(" + filterExpSql(request, filterExp.getFirstExp()) + ")";
            if (filterExp.getSequences() != null) {
                for (MakeReportRequest.AddBoolExp sequence : filterExp.getSequences()) {
                    result += boolSql(sequence.getBoolOp()) + "(" + filterExpSql(request, sequence.getAddedExp()) + ")";
                }
            }
            return result;
        }
        if (!CollectionUtils.isEmpty(filterExp.getSequences())) {
            throw new IllegalArgumentException("Only simple or FirstExp composite FilterExp compare types are supported.");
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
        Property property = propertyByToken(request, column);
        return property == null || !StringUtils.hasText(property.getColumn()) ? null : property.getColumn();
    }

    private Property propertyByToken(MakeReportRequest request, String token) {
        if (daoService == null || request.getViewId() == null || !safePropertyToken(token)) {
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
        String propertyToken = token.trim();
        return model.getProperties().stream()
                .filter(property -> matchesProperty(property, propertyToken))
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

    private String boolSql(MakeReportRequest.BoolOp boolOp) {
        String raw = "";
        if (boolOp != null) {
            raw = StringUtils.hasText(boolOp.getDbName()) ? boolOp.getDbName() : Objects.toString(boolOp.getShowName(), "");
        }
        String token = raw.trim().toLowerCase(Locale.ROOT);
        switch (token) {
            case "and":
            case "与":
            case "并且":
                return " And ";
            case "or":
            case "或":
            case "或者":
                return " OR ";
            default:
                throw new IllegalArgumentException("Only AND/OR composite FilterExp bool operators are supported.");
        }
    }

    private List<String> columns(MakeReportRequest request, ListViewResult queryResult) {
        if (!CollectionUtils.isEmpty(request.getReportCols())) {
            return request.getReportCols().stream()
                    .sorted(Comparator.comparingInt(col -> col.getIndex() == null ? 0 : col.getIndex()))
                    .map(col -> reportColumnName(request, col))
                    .filter(StringUtils::hasText)
                    .toList();
        }
        return queryResult.getCols() == null ? List.of() : queryResult.getCols();
    }

    private String reportColumnName(MakeReportRequest request, MakeReportRequest.ReportCol col) {
        if (StringUtils.hasText(col.getColName())) {
            return col.getColName();
        }
        Property property = propertyByToken(request, col.getColId());
        if (property == null) {
            return null;
        }
        return StringUtils.hasText(property.getRemark()) ? property.getRemark() : property.getName();
    }

    private List<Map<String, Object>> rows(MakeReportRequest request, ListViewResult queryResult) {
        List<ListDataItem> items = queryResult.getData() == null ? queryResult.getItems() : queryResult.getData();
        if (items == null) {
            return List.of();
        }
        return items.stream().map(item -> row(request, item)).toList();
    }

    private Map<String, Object> row(MakeReportRequest request, ListDataItem item) {
        Map<String, Object> row = row(item);
        if (CollectionUtils.isEmpty(request.getReportCols())) {
            return row;
        }
        for (MakeReportRequest.ReportCol col : request.getReportCols()) {
            String name = reportColumnName(request, col);
            if (StringUtils.hasText(name) && !row.containsKey(name)) {
                Object value = reportColumnValue(request, col, row);
                if (value != null) {
                    row.put(name, value);
                }
            }
        }
        return row;
    }

    private Object reportColumnValue(MakeReportRequest request, MakeReportRequest.ReportCol col, Map<String, Object> row) {
        Property property = propertyByToken(request, col.getColId());
        if (property == null) {
            return null;
        }
        return firstValue(row,
                property.getName(),
                property.getRemark(),
                property.getColumn(),
                property.getId() == null ? null : property.getId().toString());
    }

    private Object firstValue(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (StringUtils.hasText(key) && row.containsKey(key)) {
                return row.get(key);
            }
        }
        return null;
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
