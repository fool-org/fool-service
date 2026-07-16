package org.fool.framework.agent.action;

import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.RiskLevel;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ActionCatalog {
    private static final Map<String, Definition> DEFINITIONS = Map.ofEntries(
            Map.entry("view.discover", new Definition("View", RiskLevel.LOW, false)),
            Map.entry("view.read", new Definition("View", RiskLevel.LOW, false)),
            Map.entry("view.query", new Definition("View", RiskLevel.LOW, false)),
            Map.entry("report.preview", new Definition("View", RiskLevel.LOW, false)),
            Map.entry("report.save", new Definition("View", RiskLevel.MEDIUM, true)),
            Map.entry("report.export", new Definition("View", RiskLevel.MEDIUM, true)),
            Map.entry("data.create", new Definition("View", RiskLevel.MEDIUM, true)),
            Map.entry("data.update", new Definition("View", RiskLevel.MEDIUM, true)),
            Map.entry("data.delete", new Definition("View", RiskLevel.HIGH, true)),
            Map.entry("operation.execute", new Definition("Operation", RiskLevel.MEDIUM, true)),
            Map.entry("model.configure", new Definition("Model", RiskLevel.HIGH, false)),
            Map.entry("model.ddl.preview", new Definition("Model", RiskLevel.MEDIUM, false)),
            Map.entry("model.ddl.execute", new Definition("Model", RiskLevel.HIGH, true)),
            Map.entry("datasource.test", new Definition("DataSource", RiskLevel.MEDIUM, false)),
            Map.entry("datasource.route.update", new Definition("DataSource", RiskLevel.HIGH, true)),
            Map.entry("datasource.credential.update", new Definition("DataSource", RiskLevel.HIGH, true)),
            Map.entry("event.preview", new Definition("Event", RiskLevel.LOW, false)),
            Map.entry("event.enable", new Definition("Event", RiskLevel.HIGH, true)),
            Map.entry("message.send", new Definition("Event", RiskLevel.HIGH, true)),
            Map.entry("agent.use", new Definition("AgentCapability", RiskLevel.LOW, false)),
            Map.entry("auth.permission.change", new Definition("Auth", RiskLevel.CRITICAL, false)),
            Map.entry("action.approve", new Definition("ActionRequest", RiskLevel.HIGH, false)));

    public Definition require(String action, String resourceType) {
        Definition definition = DEFINITIONS.get(action);
        if (definition == null) {
            throw new ControlledActionException(400, "ACTION_UNKNOWN");
        }
        if (!definition.resourceType().equalsIgnoreCase(resourceType)) {
            throw new ControlledActionException(400, "ACTION_RESOURCE_MISMATCH");
        }
        return definition;
    }

    public Map<String, Definition> definitions() {
        return DEFINITIONS;
    }

    public record Definition(String resourceType, RiskLevel minimumRisk, boolean executable) {
    }
}
