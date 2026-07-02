package org.fool.framework.model.sqlscript;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.commonconst.DbConst;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SqlGenerator {

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
        var filterQuery = filter.generateSql();
        StringBuilder builder = new StringBuilder();
        List<Property> selectedProperties = selectedProperties(model, properties);
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
        if (orderColumn != null && !orderColumn.isBlank()) {
            builder.append(" ORDER BY `")
                    .append(orderColumn)
                    .append("` ")
                    .append(orderDescending ? "DESC" : "ASC");
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
                    .map(MultiDbMap::getColumnName)
                    .filter(StringUtils::hasText)
                    .toList();
        }
        if (joinsBusinessObject(property)) {
            List<String> expressions = new ArrayList<>();
            addBusinessObjectSelect(expressions, property, property.getPropertyModel().getIdProperty());
            addBusinessObjectSelect(expressions, property, showProperty(property.getPropertyModel()));
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
        return PropertyType.BusinessObject.equals(property.getPropertyType())
                && !Boolean.TRUE.equals(property.getMultiMap())
                && !Boolean.TRUE.equals(property.getIsCollection())
                && property.getPropertyModel() != null
                && property.getPropertyModel().getIdProperty() != null
                && showProperty(property.getPropertyModel()) != null
                && showProperty(property.getPropertyModel()) != property.getPropertyModel().getIdProperty()
                && StringUtils.hasText(property.getColumn())
                && StringUtils.hasText(property.getPropertyModel().getTableName());
    }

    private Property showProperty(Model model) {
        return model.getShowProperty() == null ? model.getIdProperty() : model.getShowProperty();
    }

    private String baseColumn(Model model, String column) {
        return "`" + model.getTableName() + "`.`" + column + "`";
    }

    private List<MultiDbMap> safeDbMaps(Property property) {
        return property.getDbMaps() == null ? List.of() : property.getDbMaps();
    }

    /**
     * 生成数量
     *
     * @param model
     * @param filter
     * @return
     */
    public QueryAndArgs generateSelectCount(Model model, IQueryFilter filter) {
        var filterQuery = filter.generateSql();
        StringBuilder builder = new StringBuilder();
        builder.append(DbConst.SELECT);
        builder.append(DbConst.COUNT_ONE);
        builder.append(DbConst.FROM);
        builder.append("`" + model.getTableName() + "`");
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
        var model = property.getPropertyModel();
        StringBuilder builder = new StringBuilder();
        builder.append(DbConst.SELECT);

        var columns = model.getProperties().stream().filter(p -> p.getIsCollection() == false).map(Property::getColumn).collect(Collectors.toList());
        if (!columns.contains(property.getColumn())) {
            columns.add(property.getColumn());
        }
        builder.append(columns.stream().collect(Collectors.joining(",")));


        builder.append(DbConst.FROM);
        builder.append("`" + model.getTableName() + "`");
        builder.append(DbConst.WHERE)
                .append(DbConst.AND)
                .append("`" + property.getColumn() + "` in (")
                .append(ids.stream().map(p -> "?").collect(Collectors.joining(",")))
                .append(")");
        var queryAndArgs = new QueryAndArgs();
        queryAndArgs.setArgs(ids.toArray());
        queryAndArgs.setSql(builder.toString());
        log.info("generate select sql:{}", queryAndArgs);
        return queryAndArgs;


    }
}
