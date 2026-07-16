package org.fool.framework.agent.action;

import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.RiskLevel;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ActionRiskEngineTest {
    private final ActionRiskEngine engine = new ActionRiskEngine();

    @Test
    public void riskNeverFallsBelowCatalogAndBulkMutationRaisesIt() {
        ActionCatalog.Definition medium = new ActionCatalog.Definition("View", RiskLevel.MEDIUM, true);
        ControlledActionPreview bulk = new ControlledActionPreview(
                "v1", 2, Map.of(), List.of(), "rollback", List.of());

        assertEquals(RiskLevel.HIGH, engine.assess("data.update", medium, bulk).risk());
        assertEquals(RiskLevel.MEDIUM, engine.assess("report.export", medium, bulk).risk());
    }

    @Test
    public void serverRiskFactorsRaiseRiskAndUnknownFactorFailsClosed() {
        ActionCatalog.Definition medium = new ActionCatalog.Definition("View", RiskLevel.MEDIUM, true);
        for (String factor : List.of("SENSITIVE_FIELD", "CROSS_SCOPE", "DESTRUCTIVE_WRITE",
                "NON_ROLLBACKABLE", "DDL_CHANGE", "DATASOURCE_ROUTE_CHANGE",
                "CREDENTIAL_REFERENCE_CHANGE", "SCHEDULER_CHANGE", "EXTERNAL_SIDE_EFFECT")) {
            ControlledActionPreview preview = new ControlledActionPreview(
                    "v1", 1, Map.of(), List.of(), "rollback", List.of(), List.of(factor));
            assertEquals(factor, RiskLevel.HIGH, engine.assess("data.update", medium, preview).risk());
        }

        ControlledActionPreview critical = new ControlledActionPreview(
                "v1", 1, Map.of(), List.of(), "rollback", List.of(), List.of("DESTRUCTIVE_DDL"));
        assertEquals(RiskLevel.CRITICAL, engine.assess("model.ddl.execute", medium, critical).risk());

        ControlledActionPreview unknown = new ControlledActionPreview(
                "v1", 1, Map.of(), List.of(), "rollback", List.of(), List.of("MODEL_SAID_LOW"));
        assertEquals("RISK_CALCULATION_FAILED", assertThrows(ControlledActionException.class,
                () -> engine.assess("data.update", medium, unknown)).getMessage());
    }

    @Test
    public void largeExportRaisesToHigh() {
        ActionCatalog.Definition medium = new ActionCatalog.Definition("View", RiskLevel.MEDIUM, true);
        ControlledActionPreview large = new ControlledActionPreview(
                "v1", 101, Map.of(), List.of(), "discard", List.of());

        assertEquals(RiskLevel.HIGH, engine.assess("report.export", medium, large).risk());
    }

    @Test
    public void databasePolicyCanRaiseButNeverLowerTheCodeFloor() {
        ActionCatalog.Definition medium = new ActionCatalog.Definition("Operation", RiskLevel.MEDIUM, true);
        ControlledActionPreview preview = new ControlledActionPreview(
                "v1", 1, Map.of(), List.of(), "rollback", List.of());

        assertEquals(RiskLevel.HIGH,
                engine.assess("operation.execute", medium, preview, RiskLevel.HIGH).risk());
        assertEquals(List.of("ACTION_CATALOG_FLOOR", "POLICY_RISK_FLOOR"),
                engine.assess("operation.execute", medium, preview, RiskLevel.HIGH).reasons());
        assertEquals(RiskLevel.MEDIUM,
                engine.assess("operation.execute", medium, preview, RiskLevel.LOW).risk());
    }
}
