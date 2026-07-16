package org.fool.framework.agent.service;

import org.fool.framework.common.authz.DataClassification;
import org.fool.framework.common.authz.DataPolicy;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AgentOutboundPolicyTest {
    private final AgentOutboundPolicy policy = new AgentOutboundPolicy();

    @Test
    public void removesUnauthorizedRestrictedAndExecutableFields() {
        DataPolicy dataPolicy = new DataPolicy(
                List.of(new DataPolicy.RowRule("ALL", Map.of())),
                Set.of("publicName", "token"),
                Set.of(), Set.of(), Set.of(), Set.of(), Set.of("publicName", "token"),
                Map.of(),
                Map.of("publicName", DataClassification.PUBLIC),
                Set.of("openai"),
                10,
                10);
        AgentDraft draft = new AgentDraft(
                AgentCapabilityType.REPORT_QUERY,
                "token=raw-secret",
                "fool-report",
                "low-read-only",
                Map.of(
                        "fields", List.of(
                                Map.of("PropertyName", "publicName", "Value", "visible"),
                                Map.of("PropertyName", "privateAmount", "Value", "99")),
                        "Password", "secret",
                        "ConnectionString", "jdbc:mysql://db/private",
                        "ClassName", "example.Dangerous",
                        "QueryPreview", "SELECT * FROM private"),
                List.of("Authorization: Bearer abcdefghijklmnop"));

        AgentDraft sanitized = policy.sanitize(draft, dataPolicy);
        String rendered = sanitized.getDraftPayload().toString() + sanitized.getSummary()
                + sanitized.getValidationSteps();

        assertFalse(rendered.contains("privateAmount"));
        assertFalse(rendered.contains("raw-secret"));
        assertFalse(rendered.contains("jdbc:mysql"));
        assertFalse(rendered.contains("Dangerous"));
        assertFalse(rendered.contains("SELECT"));
        assertFalse(rendered.contains("abcdefghijklmnop"));
        assertEquals(DataClassification.PUBLIC, policy.highestClassification(sanitized, dataPolicy));
    }

    @Test
    public void confidentialFieldsAreMaskedAndRequireAnExplicitProviderAllowlist() {
        DataPolicy dataPolicy = new DataPolicy(
                List.of(new DataPolicy.RowRule("ALL", Map.of())),
                Set.of("customerAmount"), Set.of(), Set.of(), Set.of(), Set.of(),
                Set.of("customerAmount"), Map.of(),
                Map.of("customerAmount", DataClassification.CONFIDENTIAL),
                Set.of("openai"), 10, 10);
        AgentDraft draft = new AgentDraft(
                AgentCapabilityType.REPORT_QUERY, "summary", "fool-report", "low-read-only",
                Map.of("fields", List.of(Map.of(
                        "PropertyName", "customerAmount", "Value", "123456"))), List.of());

        AgentDraft sanitized = policy.sanitize(draft, dataPolicy);
        String rendered = sanitized.getDraftPayload().toString();

        assertFalse(rendered.contains("123456"));
        assertTrue(rendered.contains("[REDACTED]"));
        assertEquals(DataClassification.CONFIDENTIAL,
                policy.highestClassification(sanitized, dataPolicy));
        assertTrue(dataPolicy.providerAllowed("openai", DataClassification.CONFIDENTIAL));
        DataPolicy withoutApprovedProvider = new DataPolicy(
                dataPolicy.rowRules(), dataPolicy.readableFields(), dataPolicy.filterableFields(),
                dataPolicy.sortableFields(), dataPolicy.exportableFields(), dataPolicy.writableFields(),
                dataPolicy.llmVisibleFields(), dataPolicy.maskStrategies(), dataPolicy.classifications(),
                Set.of(), dataPolicy.maxQueryRows(), dataPolicy.maxExportRows(), dataPolicy.deniedFields());
        assertFalse(withoutApprovedProvider.providerAllowed("openai", DataClassification.CONFIDENTIAL));
    }
}
