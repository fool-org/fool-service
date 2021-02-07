package org.fool.framework.model.sqlscript;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.commonconst.DbConst;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
        var filterQuery = filter.generateSql();
        StringBuilder builder = new StringBuilder();
        builder.append(DbConst.SELECT);
        builder.append(properties.stream().filter(p -> p.getIsCollection() == false).map(Property::getColumn).collect(Collectors.joining(",")));
        builder.append(DbConst.FROM);
        builder.append("`" + model.getTableName() + "`");
        builder.append(DbConst.WHERE)
                .append(DbConst.AND)
                .append(filterQuery.getSql());

        List<Object> params = new LinkedList<>();
        params.addAll(Arrays.asList(filterQuery.getArgs()));
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
        return queryAndArgs;
    }

    public QueryAndArgs generateSelect(Model model, List<Property> properties, IQueryFilter filter) {
        return generateSelect(model, properties, filter, null);
    }
}
