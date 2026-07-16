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
public class DataSourceCredentialReferenceActionHandler implements ControlledActionHandler {
    private final DataSourceActionSupport support;
    public DataSourceCredentialReferenceActionHandler(DataSourceActionSupport support) { this.support = support; }
    public String action() { return "datasource.credential.update"; }
    public String resourceType() { return "DataSource"; }
    public void preflight(ControlledActionContext context) { support.credentialPlan(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return support.credentialPlan(context).version(); }
    public ControlledActionPreview preview(ControlledActionContext context) {
        var plan = support.credentialPlan(context);
        return new ControlledActionPreview(plan.version(), 1,
                Map.of("dataSourceKey", plan.key(), "credentialReferenceWillChange", true,
                        "replacesExistingReference", plan.replacesExisting()),
                List.of("only a vault/env reference is stored", "credential material is never accepted or returned"),
                "restore the prior credential reference from the secrets inventory",
                List.of("validate connectivity through the dedicated bounded test after rotation"),
                List.of("CREDENTIAL_REFERENCE_CHANGE"));
    }
    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        support.updateCredential(context);
        return new ControlledActionResult("DATASOURCE_CREDENTIAL_REFERENCE_UPDATED",
                Map.of("updated", true, "credentialMaterialExposed", false), Map.of("count", 1));
    }
}
