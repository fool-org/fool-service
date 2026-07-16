package org.fool.framework.event.action;

import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
public class EventEnableActionHandler implements ControlledActionHandler {
    private final JdbcTemplate jdbcTemplate;
    public EventEnableActionHandler(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }
    public String action() { return "event.enable"; }
    public String resourceType() { return "Event"; }
    public void preflight(ControlledActionContext context) { plan(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return plan(context).version(); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        Plan plan = plan(context);
        return new ControlledActionPreview(plan.version(), 1,
                Map.of("eventDefinitionId", context.resourceId(), "state", Map.of("from", "STOPPED", "to", "RUNNING")),
                List.of("event definition exists and is currently stopped"),
                "set the event definition state back to STOPPED",
                List.of("enabling the definition affects future scheduler evaluations"),
                List.of("SCHEDULER_CHANGE"));
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        Plan plan = plan(context);
        requireApprovedSnapshot(context, plan.version());
        if (jdbcTemplate.update("UPDATE `SW_EVT_DEF` SET `EVTDEF_STATE` = 0 WHERE `EVTDEF_ID` = ? AND `EVTDEF_STATE` = ?",
                context.resourceId(), plan.state()) != 1) throw new ControlledActionException(409, "OBJECT_CHANGED");
        return new ControlledActionResult("EVENT_ENABLED", Map.of("enabled", true), Map.of("previousState", plan.state()));
    }

    private Plan plan(ControlledActionContext context) {
        if (context.resourceId() == null || !context.resourceId().matches("[A-Fa-f0-9-]{36}")) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        Integer state = jdbcTemplate.query("SELECT `EVTDEF_STATE` FROM `SW_EVT_DEF` WHERE `EVTDEF_ID` = ?",
                rs -> rs.next() ? rs.getInt(1) : null, context.resourceId());
        if (state == null) throw denied("RESOURCE_OUT_OF_SCOPE");
        if (state == 0) throw new ControlledActionException(409, "EVENT_ALREADY_ENABLED");
        return new Plan(state, "event:" + context.resourceId() + ":state:" + state);
    }

    private static void requireApprovedSnapshot(ControlledActionContext context, String current) {
        Object expected = context.arguments().get("_approvedSnapshotVersion");
        if (!(expected instanceof String value) || !value.equals(current)) throw new ControlledActionException(409, "OBJECT_CHANGED");
    }
    private static ControlledActionException denied(String reason) { return new ControlledActionException(400, reason); }
    private record Plan(int state, String version) {}
}
