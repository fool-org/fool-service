package org.fool.framework.agent.action;

import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.RiskLevel;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ActionRiskEngine {
    private static final Map<String, RiskLevel> FACTOR_RISKS = Map.ofEntries(
            Map.entry("SENSITIVE_FIELD", RiskLevel.HIGH),
            Map.entry("CROSS_SCOPE", RiskLevel.HIGH),
            Map.entry("DESTRUCTIVE_WRITE", RiskLevel.HIGH),
            Map.entry("NON_ROLLBACKABLE", RiskLevel.HIGH),
            Map.entry("DDL_CHANGE", RiskLevel.HIGH),
            Map.entry("DATASOURCE_ROUTE_CHANGE", RiskLevel.HIGH),
            Map.entry("CREDENTIAL_REFERENCE_CHANGE", RiskLevel.HIGH),
            Map.entry("SCHEDULER_CHANGE", RiskLevel.HIGH),
            Map.entry("EXTERNAL_SIDE_EFFECT", RiskLevel.HIGH),
            Map.entry("DESTRUCTIVE_DDL", RiskLevel.CRITICAL),
            Map.entry("UNKNOWN_SIDE_EFFECT", RiskLevel.CRITICAL),
            Map.entry("ARBITRARY_EXECUTION", RiskLevel.CRITICAL));

    public Assessment assess(String action,
                             ActionCatalog.Definition definition,
                             ControlledActionPreview preview) {
        return assess(action, definition, preview, RiskLevel.LOW);
    }

    public Assessment assess(String action,
                             ActionCatalog.Definition definition,
                             ControlledActionPreview preview,
                             RiskLevel policyMinimumRisk) {
        RiskLevel risk = RiskLevel.max(definition.minimumRisk(),
                policyMinimumRisk == null ? RiskLevel.LOW : policyMinimumRisk);
        Set<String> reasons = new LinkedHashSet<>();
        reasons.add("ACTION_CATALOG_FLOOR");
        if (policyMinimumRisk != null && policyMinimumRisk.ordinal() > definition.minimumRisk().ordinal()) {
            reasons.add("POLICY_RISK_FLOOR");
        }
        if (("data.update".equals(action) || "data.delete".equals(action))
                && preview != null && preview.affectedObjectCount() > 1) {
            risk = RiskLevel.max(risk, RiskLevel.HIGH);
            reasons.add("BULK_ROW_THRESHOLD");
        }
        if ("report.export".equals(action)
                && preview != null && preview.affectedObjectCount() > 100) {
            risk = RiskLevel.max(risk, RiskLevel.HIGH);
            reasons.add("BULK_ROW_THRESHOLD");
        }
        if (preview != null && preview.affectedObjectCount() < 0) {
            risk = RiskLevel.max(risk, RiskLevel.HIGH);
            reasons.add("AFFECTED_OBJECTS_UNKNOWN");
        }
        if (preview != null) {
            for (String factor : preview.riskFactors()) {
                RiskLevel factorRisk = FACTOR_RISKS.get(factor);
                if (factorRisk == null) {
                    throw new ControlledActionException(503, "RISK_CALCULATION_FAILED");
                }
                risk = RiskLevel.max(risk, factorRisk);
                reasons.add(factor);
            }
        }
        return new Assessment(risk, List.copyOf(reasons));
    }

    public record Assessment(RiskLevel risk, List<String> reasons) {
    }
}
