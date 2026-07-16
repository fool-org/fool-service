package org.fool.framework.dbmanage.action;

import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class DataSourceRouteActionHandler implements ControlledActionHandler {
    private final DataSourceActionSupport support;
    public DataSourceRouteActionHandler(DataSourceActionSupport support) { this.support = support; }
    public String action() { return "datasource.route.update"; }
    public String resourceType() { return "DataSource"; }
    public void preflight(ControlledActionContext context) { support.routePlan(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return support.routePlan(context).version(); }
    public ControlledActionPreview preview(ControlledActionContext context) {
        var plan = support.routePlan(context);
        return new ControlledActionPreview(plan.version(), 1,
                Map.of("dataSourceKey", plan.key(), "fromDatabaseNo", plan.current(), "toDatabaseNo", plan.target()),
                List.of("target working database is active and belongs to the current application"),
                "restore data-source route to database " + plan.current(), List.of("routing affects future requests"),
                List.of("DATASOURCE_ROUTE_CHANGE"));
    }
    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        support.updateRoute(context);
        return new ControlledActionResult("DATASOURCE_ROUTE_UPDATED", Map.of("updated", true), Map.of("count", 1));
    }
}
