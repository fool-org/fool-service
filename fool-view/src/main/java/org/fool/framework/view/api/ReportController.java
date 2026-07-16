package org.fool.framework.view.api;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.authz.AuthorizationDeniedException;
import org.fool.framework.common.authz.DataPolicy;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.JdbcCompareOpCatalog;
import org.fool.framework.query.JdbcSelectTypeCatalog;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.SelectType;
import org.fool.framework.query.SimpleFilter;
import org.fool.framework.dao.QueryAndArgs;
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
import org.fool.framework.view.service.ReadAuthorizationEnforcer;
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
    @Autowired
    private ReadAuthorizationEnforcer authorizationEnforcer;

    private final ReportGridRenderer renderer = new ReportGridRenderer();

    @PostMapping({"/getmkqview", "/mkqview"})
    @ResponseBody
    public CommonResponse<ReportModelResult> getReportModel(@RequestBody MakeReportRequest request) {
        ReportModelResult result = new ReportModelResult();
        ReportViewContext context = viewContext(request);
        if (context.model() == null || CollectionUtils.isEmpty(context.model().getProperties())) {
            return new CommonResponse<>(result);
        }
        result.setCols(reportModelCols(context));
        return new CommonResponse<>(result);
    }

    @PostMapping({"/makereport", "/getrpt", "/mkrpt"})
    @ResponseBody
    public CommonResponse<ReportGridResult> makeReport(@RequestBody MakeReportRequest request) {
        ReportViewContext context = viewContext(request);
        validateRequestedFields(request, context);
        PageNavigator page = new PageNavigator();
        page.setPageIndex(request.getCurrentPage() == null ? 0 : request.getCurrentPage());
        page.setPageSize(request.getPageSize() == null ? 0 : request.getPageSize());
        authorizationEnforcer.constrainPage(context.policy(), page);
        ListViewResult queryResult = dataQueryService.queryReportViewData(
                request.getViewId() == null ? null : request.getViewId().toString(),
                page,
                queryFilter(request, context),
                reportOrder(request, context));
        boolean countReport = singleCountReport(request, context);
        List<Map<String, Object>> reportRows = rows(request, context, queryResult, countReport);
        return new CommonResponse<>(renderer.render(
                request.getViewId() == null ? 0 : request.getViewId().intValue(),
                page.getPageIndex(),
                page.getPageSize(),
                countReport ? reportRows.size() : queryResult.getTotalItem() == null ? 0 : queryResult.getTotalItem(),
                countReport ? (reportRows.isEmpty() ? 0 : 1) : queryResult.getTotalPage() == null ? 0 : queryResult.getTotalPage(),
                columns(request, context, queryResult),
                reportRows));
    }

    @PostMapping("/saverpt")
    @ResponseBody
    public CommonResponse<Void> saveReport(@RequestBody MakeReportRequest request) {
        return new CommonResponse<>((Void) null);
    }

    private ReportViewContext viewContext(MakeReportRequest request) {
        if (daoService == null || request.getViewId() == null) {
            throw new AuthorizationDeniedException("RESOURCE_OUT_OF_SCOPE");
        }
        DataPolicy policy = authorizationEnforcer.requireView(
                "report.preview", request.getViewId().toString());
        View view = daoService.getOneDetailByKey(View.class, request.getViewId().toString());
        authorizationEnforcer.constrainView(view, policy);
        if (view == null || !StringUtils.hasText(view.getViewModel())) {
            return new ReportViewContext(view, null, policy);
        }
        return new ReportViewContext(
                view, daoService.getOneDetailByKey(Model.class, view.getViewModel()), policy);
    }

    private List<ReportModelResult.QueryCol> reportModelCols(ReportViewContext context) {
        if (context.view() != null && !CollectionUtils.isEmpty(context.view().getListItems())) {
            return context.view().getListItems().stream()
                    .sorted(Comparator.comparingInt(this::safeShowIndex))
                    .map(item -> reportModelCol(propertyForItem(context.model(), item), item))
                    .filter(Objects::nonNull)
                    .toList();
        }
        return context.model().getProperties().stream()
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

    private ReportModelResult.QueryCol reportModelCol(Property property, ViewItem item) {
        if (property == null || Boolean.TRUE.equals(property.getIsCollection()) || property.getPropertyType() == null) {
            return null;
        }
        ReportModelResult.QueryCol col = new ReportModelResult.QueryCol();
        PropertyType type = property.getPropertyType();
        col.setId(reportColumnId(property));
        col.setName(item != null && StringUtils.hasText(item.getItemName()) ? item.getItemName() : propertyShowName(property));
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

    private String reportColumnId(Property property) {
        return StringUtils.hasText(property.getName())
                ? property.getName()
                : property.getId() == null ? null : property.getId().toString();
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

    private IQueryFilter queryFilter(MakeReportRequest request, ReportViewContext context) {
        if (StringUtils.hasText(request.getQueryFilter())) {
            throw new AuthorizationDeniedException("CLIENT_RAW_FILTER_FORBIDDEN");
        }
        return filterExpFilter(context, request.getFilterExp());
    }

    private void validateRequestedFields(MakeReportRequest request, ReportViewContext context) {
        if (!CollectionUtils.isEmpty(request.getReportCols())) {
            request.getReportCols().forEach(col -> {
                Property property = propertyByToken(context, col.getColId());
                if (property == null || !authorizationEnforcer.readable(context.policy(), property)) {
                    throw new AuthorizationDeniedException("FIELD_NOT_READABLE");
                }
            });
        }
        validateFilterFields(context, request.getFilterExp());
    }

    private void validateFilterFields(ReportViewContext context, MakeReportRequest.BoolExp expression) {
        if (expression == null) {
            return;
        }
        if (expression.getFirstExp() != null) {
            validateFilterFields(context, expression.getFirstExp());
        } else if (expression.getCol() != null) {
            Property property = propertyByToken(context,
                    StringUtils.hasText(expression.getCol().getId())
                            ? expression.getCol().getId()
                            : expression.getCol().getName());
            authorizationEnforcer.requireFilterable(context.policy(), property);
        }
        if (expression.getSequences() != null) {
            expression.getSequences().forEach(sequence -> validateFilterFields(context, sequence.getAddedExp()));
        }
    }

    private IQueryFilter filterExpFilter(ReportViewContext context, MakeReportRequest.BoolExp filterExp) {
        if (filterExp == null) {
            return null;
        }
        if (filterExp.getFirstExp() != null) {
            IQueryFilter result = filterExpFilter(context, filterExp.getFirstExp());
            if (filterExp.getSequences() != null) {
                for (MakeReportRequest.AddBoolExp sequence : filterExp.getSequences()) {
                    String bool = boolSql(sequence.getBoolOp()).trim();
                    IQueryFilter added = filterExpFilter(context, sequence.getAddedExp());
                    result = "OR".equals(bool) ? result.or(added) : result.and(added);
                }
            }
            return result;
        }
        if (!CollectionUtils.isEmpty(filterExp.getSequences())) {
            throw new IllegalArgumentException("Only simple or FirstExp composite FilterExp compare types are supported.");
        }
        String column = filterColumn(context, filterExp);
        String op = compareSql(filterExp.getCompareOp());
        if (!safeColumn(column) || op == null) {
            throw new IllegalArgumentException("Only simple FilterExp compare types are supported.");
        }
        String value = filterExp.getValueExp() == null ? "" : filterExp.getValueExp();
        String parameter = " LIKE ".equals(op) ? "%" + value + "%" : value;
        String sql = "`" + column.trim() + "`" + op + "?";
        return new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs result = new QueryAndArgs();
                result.setSql(sql);
                result.setArgs(new Object[]{parameter});
                return result;
            }
        };
    }

    private String filterColumn(ReportViewContext context, MakeReportRequest.BoolExp filterExp) {
        String column = filterExp.getCol() == null ? null : filterExp.getCol().getName();
        if (!StringUtils.hasText(column) && filterExp.getCol() != null) {
            column = filterExp.getCol().getId();
        }
        String mappedColumn = mappedColumn(context, column);
        return StringUtils.hasText(mappedColumn) ? mappedColumn : column;
    }

    private String mappedColumn(ReportViewContext context, String column) {
        Property property = propertyByToken(context, column);
        return property == null || !StringUtils.hasText(property.getColumn()) ? null : property.getColumn();
    }

    private Property propertyByToken(ReportViewContext context, String token) {
        if (context == null || context.model() == null || !StringUtils.hasText(token)) {
            return null;
        }
        String propertyToken = token.trim();
        Property viewItemProperty = propertyFromViewItem(context, propertyToken);
        if (viewItemProperty != null) {
            return viewItemProperty;
        }
        if (CollectionUtils.isEmpty(context.model().getProperties())) {
            return null;
        }
        return context.model().getProperties().stream()
                .filter(property -> matchesProperty(property, propertyToken))
                .findFirst()
                .orElse(null);
    }

    private Property propertyFromViewItem(ReportViewContext context, String token) {
        if (context.view() == null || CollectionUtils.isEmpty(context.view().getListItems())) {
            return null;
        }
        for (ViewItem item : context.view().getListItems()) {
            Property property = propertyForItem(context.model(), item);
            if (property != null && matchesViewItem(item, property, token)) {
                return property;
            }
        }
        return null;
    }

    private boolean matchesViewItem(ViewItem item, Property property, String token) {
        return Objects.equals(item.getItemName(), token)
                || Objects.equals(item.getItemLabel(), token)
                || Objects.equals(item.getModelProperty(), token)
                || (item.getId() != null && Objects.equals(item.getId().toString(), token))
                || matchesProperty(property, token);
    }

    private boolean matchesProperty(Property property, String token) {
        return Objects.equals(property.getName(), token)
                || Objects.equals(property.getRemark(), token)
                || Objects.equals(property.getColumn(), token)
                || (property.getId() != null && Objects.equals(property.getId().toString(), token));
    }

    private boolean safeColumn(String column) {
        return StringUtils.hasText(column) && column.trim().matches("[A-Za-z_][A-Za-z0-9_]*");
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

    private List<String> columns(MakeReportRequest request, ReportViewContext context, ListViewResult queryResult) {
        if (!CollectionUtils.isEmpty(request.getReportCols())) {
            return request.getReportCols().stream()
                    .sorted(Comparator.comparingInt(col -> col.getIndex() == null ? 0 : col.getIndex()))
                    .map(col -> reportColumnName(context, col))
                    .filter(StringUtils::hasText)
                    .toList();
        }
        return queryResult.getCols() == null ? List.of() : queryResult.getCols();
    }

    private String reportColumnName(ReportViewContext context, MakeReportRequest.ReportCol col) {
        if (StringUtils.hasText(col.getColName())) {
            return col.getColName();
        }
        Property property = propertyByToken(context, col.getColId());
        if (property == null) {
            return null;
        }
        return StringUtils.hasText(property.getRemark()) ? property.getRemark() : property.getName();
    }

    private List<Map<String, Object>> rows(
            MakeReportRequest request,
            ReportViewContext context,
            ListViewResult queryResult,
            boolean countReport) {
        if (countReport) {
            MakeReportRequest.ReportCol col = request.getReportCols().get(0);
            String name = reportColumnName(context, col);
            return List.of(Map.of(StringUtils.hasText(name) ? name : "COUNT", countValue(queryResult)));
        }
        List<ListDataItem> items = queryResult.getData() == null ? queryResult.getItems() : queryResult.getData();
        if (items == null) {
            return List.of();
        }
        return items.stream().map(item -> row(request, context, item)).toList();
    }

    private DataQueryService.QueryOrder reportOrder(MakeReportRequest request, ReportViewContext context) {
        if (CollectionUtils.isEmpty(request.getReportCols())) {
            return null;
        }
        List<MakeReportRequest.ReportCol> sortedCols = request.getReportCols().stream()
                .sorted(Comparator.comparingInt(this::reportColIndex))
                .toList();
        List<DataQueryService.QueryOrder.Item> items = sortedCols.stream()
                .filter(item -> !nullOrder(item.getOrderType()))
                .map(col -> {
                    String token = reportOrderToken(context, col);
                    return StringUtils.hasText(token)
                            ? new DataQueryService.QueryOrder.Item(token, descOrder(col.getOrderType()))
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();
        if (!items.isEmpty()) {
            return new DataQueryService.QueryOrder(items);
        }
        String token = reportOrderToken(context, sortedCols.get(0));
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return new DataQueryService.QueryOrder(token, false);
    }

    private String reportOrderToken(ReportViewContext context, MakeReportRequest.ReportCol col) {
        Property property = propertyByToken(context, StringUtils.hasText(col.getColId()) ? col.getColId() : col.getColName());
        if (property != null && StringUtils.hasText(property.getName())) {
            return property.getName();
        }
        if (StringUtils.hasText(col.getColId())) {
            return col.getColId();
        }
        return col.getColName();
    }

    private int reportColIndex(MakeReportRequest.ReportCol col) {
        return col.getIndex() == null ? 0 : col.getIndex();
    }

    private boolean nullOrder(String orderType) {
        return !StringUtils.hasText(orderType) || "2".equals(orderType.trim()) || "NULL".equalsIgnoreCase(orderType.trim());
    }

    private boolean descOrder(String orderType) {
        return StringUtils.hasText(orderType) && ("1".equals(orderType.trim()) || "DESC".equalsIgnoreCase(orderType.trim()));
    }

    private boolean singleCountReport(MakeReportRequest request, ReportViewContext context) {
        if (CollectionUtils.isEmpty(request.getReportCols()) || request.getReportCols().size() != 1) {
            return false;
        }
        return isCountSelectType(selectedType(context, request.getReportCols().get(0)));
    }

    private SelectType selectedType(ReportViewContext context, MakeReportRequest.ReportCol col) {
        Property property = propertyByToken(context, col.getColId());
        if (selectTypeCatalog == null
                || property == null
                || property.getPropertyType() == null
                || !StringUtils.hasText(col.getSelectedTypeId())) {
            return null;
        }
        return selectTypeCatalog.listFor(property.getPropertyType()).stream()
                .filter(type -> Objects.equals(Long.toString(type.getId()), col.getSelectedTypeId().trim()))
                .findFirst()
                .orElse(null);
    }

    private boolean isCountSelectType(SelectType selectType) {
        return selectType != null
                && StringUtils.hasText(selectType.getDbExp())
                && selectType.getDbExp().trim().toUpperCase(Locale.ROOT).startsWith("COUNT(");
    }

    private long countValue(ListViewResult queryResult) {
        if (queryResult.getTotalItem() != null) {
            return queryResult.getTotalItem();
        }
        List<ListDataItem> items = queryResult.getData() == null ? queryResult.getItems() : queryResult.getData();
        return items == null ? 0 : items.size();
    }

    private Map<String, Object> row(MakeReportRequest request, ReportViewContext context, ListDataItem item) {
        Map<String, Object> row = row(item);
        if (CollectionUtils.isEmpty(request.getReportCols())) {
            return row;
        }
        for (MakeReportRequest.ReportCol col : request.getReportCols()) {
            String name = reportColumnName(context, col);
            if (StringUtils.hasText(name) && !row.containsKey(name)) {
                Object value = reportColumnValue(context, col, row);
                if (value != null) {
                    row.put(name, value);
                }
            }
        }
        return row;
    }

    private Object reportColumnValue(ReportViewContext context, MakeReportRequest.ReportCol col, Map<String, Object> row) {
        Property property = propertyByToken(context, col.getColId());
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

    private record ReportViewContext(View view, Model model, DataPolicy policy) {
        private static ReportViewContext empty() {
            return new ReportViewContext(null, null, DataPolicy.unrestricted());
        }
    }
}
