package org.fool.framework.model.sqlscript;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.commonconst.DbConst;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;
import org.fool.framework.model.service.ModelDisplayProperties;
import org.fool.framework.query.IQueryFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SqlGenerator {
    public static final String ITEM_PARENT_ID_COLUMN = "__parent_id";

    public record OrderColumn(String column, boolean descending) {
    }

    /**
     * 生成统一查询
     *
     * @param model
     * @param properties
     * @param filter
     * @return
     */
    public QueryAndArgs generateSelect(Model model, List<Property> properties, IQueryFilter filter, PageNavigator pageNavigator) {
        return generateSelect(model, properties, filter, pageNavigator, null, false);
    }

    public QueryAndArgs generateSelect(
            Model model,
            List<Property> properties,
            IQueryFilter filter,
            PageNavigator pageNavigator,
            String orderColumn,
            boolean orderDescending) {
        return generateSelect(
                model,
                properties,
                filter,
                pageNavigator,
                StringUtils.hasText(orderColumn) ? List.of(new OrderColumn(orderColumn, orderDescending)) : List.of());
    }

    public QueryAndArgs generateSelect(
            Model model,
            List<Property> properties,
            IQueryFilter filter,
            PageNavigator pageNavigator,
            List<OrderColumn> orderColumns) {
        var filterQuery = filter.generateSql();
        StringBuilder builder = new StringBuilder();
        List<Property> selectedProperties = selectedProperties(model, properties);
        List<OrderColumn> selectedOrderColumns = selectedOrderColumns(orderColumns);
        boolean hasJoins = selectedProperties.stream().anyMatch(this::joinsBusinessObject);
        builder.append(DbConst.SELECT);
        builder.append(selectedProperties.stream()
                .flatMap(property -> selectExpressions(model, property, hasJoins).stream())
                .collect(Collectors.joining(",")));


        builder.append(DbConst.FROM);
        builder.append("`" + model.getTableName() + "`");
        for (Property property : selectedProperties) {
            String join = joinExpression(model, property);
            if (StringUtils.hasText(join)) {
                builder.append(" ").append(join);
            }
        }
        builder.append(DbConst.WHERE)
                .append(DbConst.AND)
                .append(filterQuery.getSql());

        List<Object> params = new LinkedList<>();
        params.addAll(Arrays.asList(filterQuery.getArgs()));
        if (!selectedOrderColumns.isEmpty()) {
            builder.append(" ORDER BY ")
                    .append(selectedOrderColumns.stream()
                            .map(this::orderExpression)
                            .collect(Collectors.joining(",")));
        }
        if (pageNavigator != null) {
            builder.append(DbConst.PAGE_INFO);
            params.add(pageNavigator.getPageSize());
            params.add(pageNavigator.getPageSize() * (pageNavigator.getPageIndex() - 1));
        }
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(params.toArray());
        log.info("generate select sql:{}", queryAndArgs);
        return queryAndArgs;
    }

    private List<OrderColumn> selectedOrderColumns(List<OrderColumn> orderColumns) {
        if (orderColumns == null) {
            return List.of();
        }
        return orderColumns.stream()
                .filter(Objects::nonNull)
                .filter(orderColumn -> StringUtils.hasText(orderColumn.column()))
                .toList();
    }

    private List<Property> selectedProperties(Model model, List<Property> properties) {
        return properties.stream()
                .filter(property -> !Boolean.TRUE.equals(property.getIsCollection())
                        || (model.getIdProperty() != null
                        && model.getIdProperty().getColumn().equals(property.getColumn())))
                .toList();
    }

    private List<String> selectExpressions(Model model, Property property, boolean qualifyBaseColumns) {
        if (Boolean.TRUE.equals(property.getMultiMap())) {
            return safeDbMaps(property).stream()
                    .filter(map -> map != null)
                    .filter(map -> StringUtils.hasText(map.getColumnName()))
                    .map(map -> baseColumn(model, map.getColumnName()) + " AS `" + property.getName() + "_"
                            + (StringUtils.hasText(map.getPropertyName()) ? map.getPropertyName() : map.getColumnName()) + "`")
                    .toList();
        }
        if (joinsBusinessObject(property)) {
            List<String> expressions = new ArrayList<>();
            addBusinessObjectSelect(expressions, property, property.getPropertyModel().getIdProperty());
            addBusinessObjectSelect(expressions, property, ModelDisplayProperties.displayProperty(property.getPropertyModel()));
            return expressions;
        }
        if (PropertyType.BusinessObject.equals(property.getPropertyType())
                && property.getPropertyModel() != null
                && property.getPropertyModel().getIdProperty() != null
                && StringUtils.hasText(property.getColumn())) {
            return List.of(baseColumn(model, property.getColumn())
                    + " AS `" + property.getName() + "_" + property.getPropertyModel().getIdProperty().getColumn() + "`");
        }
        if (!StringUtils.hasText(property.getColumn())) {
            return List.of();
        }
        if (qualifyBaseColumns) {
            return List.of(baseColumn(model, property.getColumn()) + " AS `" + property.getColumn() + "`");
        }
        return List.of(property.getColumn());
    }

    private void addBusinessObjectSelect(List<String> expressions, Property property, Property targetProperty) {
        if (targetProperty == null || !StringUtils.hasText(targetProperty.getColumn())) {
            return;
        }
        String expression = "`" + property.getName() + "`.`" + targetProperty.getColumn() + "` AS `"
                + property.getName() + "_" + targetProperty.getColumn() + "`";
        if (!expressions.contains(expression)) {
            expressions.add(expression);
        }
    }

    private String joinExpression(Model model, Property property) {
        if (!joinsBusinessObject(property)) {
            return "";
        }
        Model targetModel = property.getPropertyModel();
        return "LEFT OUTER JOIN `" + targetModel.getTableName() + "` AS `" + property.getName() + "`"
                + " ON `" + property.getName() + "`.`" + targetModel.getIdProperty().getColumn() + "`="
                + baseColumn(model, property.getColumn());
    }

    private boolean joinsBusinessObject(Property property) {
        Property displayProperty = property.getPropertyModel() == null
                ? null
                : ModelDisplayProperties.displayProperty(property.getPropertyModel());
        return PropertyType.BusinessObject.equals(property.getPropertyType())
                && !Boolean.TRUE.equals(property.getMultiMap())
                && !Boolean.TRUE.equals(property.getIsCollection())
                && property.getPropertyModel() != null
                && property.getPropertyModel().getIdProperty() != null
                && displayProperty != null
                && !ModelDisplayProperties.sameProperty(displayProperty, property.getPropertyModel().getIdProperty())
                && StringUtils.hasText(property.getColumn())
                && StringUtils.hasText(property.getPropertyModel().getTableName());
    }

    private String baseColumn(Model model, String column) {
        return "`" + model.getTableName() + "`.`" + column + "`";
    }

    private List<MultiDbMap> safeDbMaps(Property property) {
        return property.getDbMaps() == null ? List.of() : property.getDbMaps();
    }

    private String orderExpression(String orderColumn) {
        return orderColumn.startsWith("`") ? orderColumn : "`" + orderColumn + "`";
    }

    private String orderExpression(OrderColumn orderColumn) {
        return orderExpression(orderColumn.column()) + " " + (orderColumn.descending() ? "DESC" : "ASC");
    }

    /**
     * 生成数量
     *
     * @param model
     * @param filter
     * @return
     */
    public QueryAndArgs generateSelectCount(Model model, IQueryFilter filter) {
        return generateSelectCount(model, filter, List.of());
    }

    public QueryAndArgs generateSelectCount(Model model, IQueryFilter filter, List<Property> properties) {
        var filterQuery = filter.generateSql();
        StringBuilder builder = new StringBuilder();
        List<Property> selectedProperties = selectedProperties(model, properties);
        builder.append(DbConst.SELECT);
        builder.append(DbConst.COUNT_ONE);
        builder.append(DbConst.FROM);
        builder.append("`" + model.getTableName() + "`");
        for (Property property : selectedProperties) {
            String join = joinExpression(model, property);
            if (StringUtils.hasText(join)) {
                builder.append(" ").append(join);
            }
        }
        builder.append(DbConst.WHERE)
                .append(DbConst.AND)
                .append(filterQuery.getSql());
        QueryAndArgs queryAndArgs = new QueryAndArgs();
        queryAndArgs.setSql(builder.toString());
        queryAndArgs.setArgs(filterQuery.getArgs());
        log.info("generate select sql:{}", queryAndArgs);
        return queryAndArgs;
    }

    public QueryAndArgs generateSelect(Model model, List<Property> properties, IQueryFilter filter) {
        return generateSelect(model, properties, filter, null);
    }

    public QueryAndArgs generateItems(Property property, List<String> ids) {
        return generateItems(property.getOwner(), property, ids);
    }

    public QueryAndArgs generateItems(Model parentModel, Property property, List<String> ids) {
        var model = property.getPropertyModel();
        Relation relation = itemRelation(parentModel, property);
        String parentColumn = itemParentColumn(property, relation);
        StringBuilder builder = new StringBuilder();
        builder.append(DbConst.SELECT);

        var columns = itemColumns(model, relation != null);
        if (!columns.contains(parentColumn) && relation == null) {
            columns.add(parentColumn);
        }
        builder.append(String.join(",", columns));
        if (relation == null) {
            builder.append(",`").append(parentColumn).append("` AS `").append(ITEM_PARENT_ID_COLUMN).append("`");
        } else {
            builder.append(",`")
                    .append(relation.getRelationTable())
                    .append("`.`")
                    .append(parentColumn)
                    .append("` AS `")
                    .append(ITEM_PARENT_ID_COLUMN)
                    .append("`");
        }
        builder.append(DbConst.FROM);
        builder.append("`" + model.getTableName() + "`");
        appendItemJoin(builder, model, relation);
        builder.append(DbConst.WHERE)
                .append(DbConst.AND)
                .append(itemWhereColumn(property, relation))
                .append(" in (")
                .append(ids.stream().map(p -> "?").collect(Collectors.joining(",")))
                .append(")");
        var queryAndArgs = new QueryAndArgs();
        queryAndArgs.setArgs(ids.toArray());
        queryAndArgs.setSql(builder.toString());
        log.info("generate select sql:{}", queryAndArgs);
        return queryAndArgs;


    }

    private List<String> itemColumns(Model model, boolean qualify) {
        return model.getProperties().stream()
                .filter(property -> !Boolean.TRUE.equals(property.getIsCollection()))
                .flatMap(property -> itemColumnExpressions(model, property, qualify).stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> itemColumnExpressions(Model model, Property property, boolean qualify) {
        if (Boolean.TRUE.equals(property.getMultiMap())) {
            return safeDbMaps(property).stream()
                    .filter(map -> map != null)
                    .filter(map -> StringUtils.hasText(map.getColumnName()))
                    .map(map -> itemColumnExpression(
                            model,
                            map.getColumnName(),
                            property.getName() + "_"
                                    + (StringUtils.hasText(map.getPropertyName())
                                    ? map.getPropertyName()
                                    : map.getColumnName()),
                            qualify))
                    .toList();
        }
        if (!StringUtils.hasText(property.getColumn())) {
            return List.of();
        }
        return List.of(itemColumnExpression(model, property.getColumn(), property.getColumn(), qualify));
    }

    private String itemColumnExpression(Model model, String column, String alias, boolean qualify) {
        String expression = qualify ? "`" + model.getTableName() + "`.`" + column + "`" : column;
        return qualify || !Objects.equals(column, alias) ? expression + " AS `" + alias + "`" : expression;
    }

    private Relation itemRelation(Model parentModel, Property property) {
        if (parentModel == null || parentModel.getRelations() == null) {
            return null;
        }
        return parentModel.getRelations().stream()
                .filter(relation -> sameProperty(relation.getProperty(), property))
                .findFirst()
                .orElse(null);
    }

    private boolean sameProperty(Property left, Property right) {
        return left == right
                || (left != null && right != null
                && (Objects.equals(left.getId(), right.getId()) || Objects.equals(left.getName(), right.getName())));
    }

    private String itemParentColumn(Property property, Relation relation) {
        if (relation == null || relation.getRelationType() == null) {
            return property.getColumn();
        }
        if (relation.getRelationType() == RelationType.Recurve) {
            return relation.getPropertyColumn();
        }
        return relation.getTargetColumn();
    }

    private String itemWhereColumn(Property property, Relation relation) {
        if (relation == null) {
            return "`" + property.getColumn() + "`";
        }
        return "`" + relation.getRelationTable() + "`.`" + itemParentColumn(property, relation) + "`";
    }

    private void appendItemJoin(StringBuilder builder, Model model, Relation relation) {
        if (relation == null || relation.getRelationType() == RelationType.One2Many) {
            return;
        }
        builder.append(" JOIN `")
                .append(relation.getRelationTable())
                .append("` ON `")
                .append(relation.getRelationTable())
                .append("`.`")
                .append(itemChildColumn(relation))
                .append("`=`")
                .append(model.getTableName())
                .append("`.`")
                .append(keyColumn(model))
                .append("`");
    }

    private String itemChildColumn(Relation relation) {
        return relation.getRelationType() == RelationType.Recurve
                ? relation.getTargetColumn()
                : relation.getPropertyColumn();
    }

    private String keyColumn(Model model) {
        if (model.getIdProperty() != null && StringUtils.hasText(model.getIdProperty().getColumn())) {
            return model.getIdProperty().getColumn();
        }
        return "SYSID";
    }
}
