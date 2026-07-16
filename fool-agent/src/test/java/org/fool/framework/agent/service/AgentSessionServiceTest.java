package org.fool.framework.agent.service;

import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.AuthorizationDecision;
import org.fool.framework.common.authz.AuthorizationService;
import org.fool.framework.common.authz.DataPolicy;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class AgentSessionServiceTest {
    private static final EffectiveSubject SUBJECT = subject("user-1", "fool-service", "car_wash");
    private static final EffectiveSubject OTHER_USER = subject("user-2", "fool-service", "car_wash");

    private final AgentSessionService service = service();

    @Test
    public void capabilitiesExposeConfiguredOrder() {
        var capabilities = service.capabilities();

        assertEquals("report-query", capabilities.get(0).getId());
        assertEquals("form-view", capabilities.get(1).getId());
        assertEquals("model", capabilities.get(2).getId());
        assertEquals("data-source", capabilities.get(3).getId());
        assertEquals("event-automation", capabilities.get(4).getId());
    }

    @Test
    public void sessionStartsAtReportQueryAndRecordsMessages() {
        AgentSession session = service.start(SUBJECT, "配置客户报表");

        assertEquals(AgentCapabilityType.REPORT_QUERY, session.getCurrentCapability());
        assertEquals(1, session.getMessages().size());

        AgentTurnResult result = service.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.REPORT_QUERY,
                "需要客户订单统计报表");

        assertTrue(result.isReadyToAdvance());
        assertEquals(3, result.getSession().getMessages().size());
        assertEquals(AgentMessageRole.AGENT, result.getAgentMessage().getRole());
        assertEquals(AgentCapabilityType.REPORT_QUERY, result.getDraft().getCapability());
    }

    @Test
    public void sessionPersistsRedactedUserAndProviderText() {
        AgentSession session = service.start(SUBJECT, "sensitive input");

        AgentTurnResult result = service.recordUserMessage(
                session.getId(), SUBJECT, AgentCapabilityType.REPORT_QUERY,
                "token=raw-token-123 Authorization: Bearer abcdefghijklmnop");

        String persistedUser = result.getSession().getMessages().get(1).getContent();
        assertTrue(persistedUser.contains("[REDACTED]"));
        assertTrue(!persistedUser.contains("raw-token-123"));
        assertTrue(!persistedUser.contains("abcdefghijklmnop"));
    }

    @Test
    public void reportQueryTurnReturnsReadOnlyDraft() {
        AgentSession session = service.start(SUBJECT, "配置客户报表");

        AgentTurnResult result = service.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.REPORT_QUERY,
                "需要客户订单统计报表",
                Map.of("ViewId", 100));

        assertEquals("low-read-only", result.getDraft().getRiskLevel());
        assertEquals("/api/v1/report/getmkqview", result.getDraft().getDraftPayload().get("reportModelEndpoint"));
        @SuppressWarnings("unchecked")
        Map<String, Object> draftRequest = (Map<String, Object>) result.getDraft().getDraftPayload().get("draftRequest");
        assertEquals(100, draftRequest.get("ViewId"));
    }

    @Test
    public void reportQueryTurnHydratesViewModelColumnsWhenMetadataExists() {
        AgentSessionService hydratedService = service(viewMetadataProvider());
        AgentSession session = hydratedService.start(SUBJECT, "配置客户报表");

        AgentTurnResult result = hydratedService.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.REPORT_QUERY,
                "需要客户订单统计报表",
                Map.of("ViewId", "100"));

        Map<String, Object> payload = result.getDraft().getDraftPayload();
        assertEquals("hydrated", payload.get("metadataStatus"));
        @SuppressWarnings("unchecked")
        Map<String, Object> view = (Map<String, Object>) payload.get("view");
        assertEquals("OrderList", view.get("ViewName"));
        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) payload.get("model");
        assertEquals("Order", model.get("ModelName"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidateColumns = (List<Map<String, Object>>) payload.get("candidateColumns");
        assertEquals(2, candidateColumns.size());
        assertEquals(true, candidateColumns.get(0).get("Reportable"));
        assertEquals(false, candidateColumns.get(1).get("Reportable"));
        @SuppressWarnings("unchecked")
        Map<String, Object> draftRequest = (Map<String, Object>) payload.get("draftRequest");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reportCols = (List<Map<String, Object>>) draftRequest.get("ReportCols");
        assertEquals(1, reportCols.size());
        assertEquals("Symbol", reportCols.get(0).get("ColName"));
        assertEquals("symbol", reportCols.get(0).get("ColId"));
        @SuppressWarnings("unchecked")
        Map<String, Object> previewGate = (Map<String, Object>) payload.get("previewGate");
        assertEquals("PASSED", previewGate.get("Status"));
    }

    @Test
    public void reportAgentMetadataUsesTheAuthorizationDataPolicy() {
        DataPolicy limited = new DataPolicy(
                List.of(new DataPolicy.RowRule("ALL", Map.of())),
                java.util.Set.of("symbol"),
                java.util.Set.of("symbol"),
                java.util.Set.of("symbol"),
                java.util.Set.of("symbol"),
                java.util.Set.of(),
                java.util.Set.of("symbol"),
                Map.of(), Map.of(), java.util.Set.of("local"), 10, 10);
        AuthorizationService authorization = request -> AuthorizationDecision.allow(
                request.subject().policyVersion(), "limited",
                "report.preview".equals(request.action()) ? limited : DataPolicy.unrestricted());
        AgentSessionService limitedService = new AgentSessionService(
                new InMemoryAgentSessionStore(),
                viewMetadataProvider(),
                ModelMetadataProvider.unavailable(),
                DataSourceMetadataProvider.unavailable(),
                EventAutomationMetadataProvider.unavailable(),
                TableSchemaProvider.unavailable(),
                null,
                new AgentOutboundPolicy(),
                authorization,
                event -> { },
                Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC));
        AgentSession session = limitedService.start(SUBJECT, "limited report");

        AgentTurnResult result = limitedService.recordUserMessage(
                session.getId(), SUBJECT, AgentCapabilityType.REPORT_QUERY, "preview", Map.of("ViewId", 100));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>)
                result.getDraft().getDraftPayload().get("candidateColumns");
        assertEquals(1, columns.size());
        assertEquals("symbol", columns.get(0).get("PropertyName"));
    }

    @Test
    public void formViewTurnHydratesFieldsChildCollectionsAndOperationsWhenMetadataExists() {
        AgentSessionService hydratedService = service(viewMetadataProvider());
        AgentSession session = hydratedService.start(SUBJECT, "配置订单视图");
        hydratedService.advance(session.getId(), SUBJECT);

        AgentTurnResult result = hydratedService.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.FORM_VIEW,
                "需要订单列表和详情表单",
                Map.of("ViewId", 100));

        assertEquals("medium-draft-only", result.getDraft().getRiskLevel());
        Map<String, Object> payload = result.getDraft().getDraftPayload();
        assertEquals("hydrated", payload.get("metadataStatus"));
        assertEquals("/api/v1/view/getlistview", payload.get("listViewEndpoint"));
        assertEquals("/api/v1/data/runoperation", payload.get("runOperationEndpoint"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fields = (List<Map<String, Object>>) payload.get("fields");
        assertEquals(1, fields.size());
        assertEquals("Symbol", fields.get(0).get("Name"));
        assertEquals(true, fields.get(0).get("Readonly"));
        assertEquals("readonly-field", fields.get(0).get("DraftControlType"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> childCollections =
                (List<Map<String, Object>>) payload.get("childCollections");
        assertEquals(1, childCollections.size());
        assertEquals(101L, childCollections.get(0).get("ListViewId"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) payload.get("operations");
        assertEquals(1, operations.size());
        assertEquals(7001L, operations.get(0).get("OperationId"));
        assertEquals("COMMAND", operations.get(0).get("Type"));
        assertEquals(true, operations.get(0).get("RequireSelect"));
        assertEquals("确定要删除？", operations.get(0).get("ConfirmMessage"));

        @SuppressWarnings("unchecked")
        Map<String, Object> draftView = (Map<String, Object>) payload.get("draftView");
        assertEquals(102L, draftView.get("DetailViewId"));
        assertEquals(fields, draftView.get("Fields"));
        assertEquals(childCollections, draftView.get("ChildCollections"));
        assertEquals(operations, draftView.get("Operations"));
        @SuppressWarnings("unchecked")
        Map<String, Object> previewGate = (Map<String, Object>) payload.get("previewGate");
        assertEquals("PASSED", previewGate.get("Status"));
    }

    @Test
    public void modelTurnHydratesPropertiesRelationsOperationsAndDdlDryRunPlan() {
        AgentSessionService hydratedService = service(
                viewMetadataProvider(),
                modelMetadataProvider(),
                DataSourceMetadataProvider.unavailable(),
                EventAutomationMetadataProvider.unavailable(),
                tableName -> TableSchemaSnapshot.hydrated(java.util.Set.of("order_id", "items")));
        AgentSession session = hydratedService.start(SUBJECT, "配置订单模型");
        hydratedService.advance(session.getId(), SUBJECT);
        hydratedService.advance(session.getId(), SUBJECT);

        AgentTurnResult result = hydratedService.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.MODEL,
                "需要订单模型字段、关系和 DDL dry-run",
                Map.of("ModelId", 100));

        assertEquals("high-dry-run-required", result.getDraft().getRiskLevel());
        Map<String, Object> payload = result.getDraft().getDraftPayload();
        assertEquals("hydrated", payload.get("metadataStatus"));
        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) payload.get("model");
        assertEquals("Order", model.get("ModelName"));
        assertEquals("market_order", model.get("TableName"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> properties = (List<Map<String, Object>>) payload.get("properties");
        assertEquals(2, properties.size());
        assertEquals("orderId", properties.get(0).get("Name"));
        assertEquals("order_id", properties.get(0).get("DbColumn"));
        assertEquals("check-column", properties.get(0).get("DdlCheck"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> relations = (List<Map<String, Object>>) payload.get("relations");
        assertEquals(1, relations.size());
        assertEquals(1004L, relations.get(0).get("SourcePropertyId"));
        assertEquals("market_order_item", relations.get(0).get("TableName"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> operations = (List<Map<String, Object>>) payload.get("operations");
        assertEquals(1, operations.size());
        assertEquals(7002L, operations.get(0).get("OperationId"));
        assertEquals("保存", operations.get(0).get("Name"));
        assertEquals(1, operations.get(0).get("CommandCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> ddlDryRunPlan = (Map<String, Object>) payload.get("ddlDryRunPlan");
        assertEquals("read-only-ddl-diff", ddlDryRunPlan.get("Mode"));
        assertEquals("market_order", ddlDryRunPlan.get("TableName"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columnChecks =
                (List<Map<String, Object>>) ddlDryRunPlan.get("ColumnChecks");
        assertEquals(2, columnChecks.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> previewGate = (Map<String, Object>) ddlDryRunPlan.get("PreviewGate");
        assertEquals("PASSED", previewGate.get("Status"));
    }

    @Test
    public void dataSourceTurnHydratesRoutesAndCredentialBoundary() {
        AgentSessionService hydratedService = service(
                viewMetadataProvider(),
                modelMetadataProvider(),
                dataSourceMetadataProvider(),
                EventAutomationMetadataProvider.unavailable(),
                TableSchemaProvider.unavailable());
        AgentSession session = hydratedService.start(SUBJECT, "配置订单数据源");
        hydratedService.advance(session.getId(), SUBJECT);
        hydratedService.advance(session.getId(), SUBJECT);
        hydratedService.advance(session.getId(), SUBJECT);

        AgentTurnResult result = hydratedService.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.DATA_SOURCE,
                "需要绑定 car_wash 数据源",
                Map.of("DataSourceKey", "car_wash"));

        assertEquals("high-credential-boundary", result.getDraft().getRiskLevel());
        Map<String, Object> payload = result.getDraft().getDraftPayload();
        assertEquals("hydrated", payload.get("metadataStatus"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> workingDatabases =
                (List<Map<String, Object>>) payload.get("workingDatabases");
        assertEquals(1, workingDatabases.size());
        assertEquals("01", workingDatabases.get(0).get("DbNo"));
        assertEquals(true, workingDatabases.get(0).get("CredentialConfigured"));
        assertEquals(false, workingDatabases.get(0).containsKey("Password"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dataSourceRoutes =
                (List<Map<String, Object>>) payload.get("dataSourceRoutes");
        assertEquals(1, dataSourceRoutes.size());
        assertEquals("car_wash", dataSourceRoutes.get(0).get("DataSourceKey"));

        @SuppressWarnings("unchecked")
        Map<String, Object> selectedRoute = (Map<String, Object>) payload.get("selectedRoute");
        assertEquals("dataSourceKey", selectedRoute.get("SelectionType"));
        assertEquals("car_wash", selectedRoute.get("DataSourceKey"));
        assertEquals("01", selectedRoute.get("DbNo"));

        @SuppressWarnings("unchecked")
        Map<String, Object> credentialPolicy = (Map<String, Object>) payload.get("credentialPolicy");
        assertEquals(false, credentialPolicy.get("PlaintextSecretsExposed"));
        assertEquals(false, credentialPolicy.get("RawConnectionStringExposed"));

        @SuppressWarnings("unchecked")
        Map<String, Object> connectivityCheckPlan =
                (Map<String, Object>) payload.get("connectivityCheckPlan");
        assertEquals("read-only-connectivity-check", connectivityCheckPlan.get("Mode"));
        assertEquals("SELECT 1", connectivityCheckPlan.get("Query"));
        assertEquals(false, connectivityCheckPlan.get("PlaintextSecretsExposed"));
        assertEquals("WorkDataBase.DBNo=01/pwd*", connectivityCheckPlan.get("CredentialReference"));
    }

    @Test
    public void eventAutomationTurnHydratesDefinitionsDryRunAndAuditPlan() {
        AgentSessionService hydratedService = service(
                viewMetadataProvider(),
                modelMetadataProvider(),
                dataSourceMetadataProvider(),
                eventAutomationMetadataProvider(),
                TableSchemaProvider.unavailable());
        AgentSession session = hydratedService.start(SUBJECT, "配置订单事件");
        hydratedService.advance(session.getId(), SUBJECT);
        hydratedService.advance(session.getId(), SUBJECT);
        hydratedService.advance(session.getId(), SUBJECT);
        hydratedService.advance(session.getId(), SUBJECT);

        AgentTurnResult result = hydratedService.recordUserMessage(
                session.getId(),
                SUBJECT,
                AgentCapabilityType.EVENT_AUTOMATION,
                "需要订单状态事件",
                Map.of("EventDefinitionId", "00000000-0000-0000-0000-000000000100"));

        assertEquals("high-dry-run-required", result.getDraft().getRiskLevel());
        Map<String, Object> payload = result.getDraft().getDraftPayload();
        assertEquals("hydrated", payload.get("metadataStatus"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> definitions =
                (List<Map<String, Object>>) payload.get("eventDefinitions");
        assertEquals(1, definitions.size());
        assertEquals("00000000-0000-0000-0000-000000000100", definitions.get(0).get("DefinitionId"));
        assertEquals(true, definitions.get(0).get("Running"));
        assertEquals(false, definitions.get(0).containsKey("QueryPreview"));

        @SuppressWarnings("unchecked")
        Map<String, Object> recipientSummary =
                (Map<String, Object>) definitions.get(0).get("RecipientSummary");
        assertEquals(1, recipientSummary.get("DirectUsers"));
        assertEquals("All authorized users", recipientSummary.get("FallbackWhenEmpty"));

        @SuppressWarnings("unchecked")
        Map<String, Object> selectedDefinition = (Map<String, Object>) payload.get("selectedDefinition");
        assertEquals("read-order", selectedDefinition.get("OperationId"));
        assertEquals("order_id", selectedDefinition.get("ObjectIdColumn"));

        @SuppressWarnings("unchecked")
        Map<String, Object> dryRunPlan = (Map<String, Object>) payload.get("dryRunPlan");
        assertEquals("read-only-matched-object-dry-run", dryRunPlan.get("Mode"));
        assertEquals(false, dryRunPlan.get("SchedulerMutation"));
        assertEquals("SW_EVT_EVENT.EVT_Defination + SW_EVT_EVENT.EVT_DEF", dryRunPlan.get("IdempotencyCheck"));

        @SuppressWarnings("unchecked")
        Map<String, Object> auditPlan = (Map<String, Object>) payload.get("auditPlan");
        assertEquals("SW_EVT_EVENT", auditPlan.get("EventTable"));
        assertEquals(0, auditPlan.get("ExistingEventCount"));
    }

    @Test
    public void sessionCannotSkipCapabilityOrder() {
        AgentSession session = service.start(SUBJECT, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.recordUserMessage(session.getId(), SUBJECT, AgentCapabilityType.MODEL, "先建模型"));

        assertTrue(error.getMessage().contains("Current capability is report-query"));
    }

    @Test
    public void sessionAdvancesInConfiguredOrderAndCompletesAtEnd() {
        AgentSession session = service.start(SUBJECT, null);

        assertEquals(AgentCapabilityType.FORM_VIEW, service.advance(session.getId(), SUBJECT).getCurrentCapability());
        assertEquals(AgentCapabilityType.MODEL, service.advance(session.getId(), SUBJECT).getCurrentCapability());
        assertEquals(AgentCapabilityType.DATA_SOURCE, service.advance(session.getId(), SUBJECT).getCurrentCapability());
        assertEquals(AgentCapabilityType.EVENT_AUTOMATION, service.advance(session.getId(), SUBJECT).getCurrentCapability());

        AgentSession completed = service.advance(session.getId(), SUBJECT);

        assertEquals(AgentSessionStatus.COMPLETED, completed.getStatus());
    }

    @Test
    public void sessionOwnerAndScopeMustMatch() {
        AgentSession session = service.start(SUBJECT, null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.get(session.getId(), OTHER_USER));

        assertEquals("AGENT_SESSION_OUT_OF_SCOPE", error.getMessage());
    }

    @Test
    public void sessionPersistsEffectiveOwnerAndScope() {
        AgentSession session = service.start(SUBJECT, null);

        assertEquals("user-1", session.getOwnerUserId());
        assertEquals("fool-service", session.getAppId());
        assertEquals("car_wash", session.getDatabaseId());
        assertEquals("auth-user-1", session.getAuthSessionId());
    }

    private static EffectiveSubject subject(String userId, String appId, String databaseId) {
        return new EffectiveSubject(
                userId,
                List.of("admin"),
                "company-1",
                List.of("department-1"),
                appId,
                databaseId,
                "auth-" + userId,
                Instant.parse("2026-07-15T00:00:00Z"),
                null,
                1L);
    }

    private AgentSessionService service() {
        return service(ReportQueryMetadataProvider.unavailable());
    }

    private AgentSessionService service(ReportQueryMetadataProvider reportProvider) {
        return service(
                reportProvider,
                ModelMetadataProvider.unavailable(),
                DataSourceMetadataProvider.unavailable(),
                EventAutomationMetadataProvider.unavailable(),
                TableSchemaProvider.unavailable());
    }

    private AgentSessionService service(ReportQueryMetadataProvider reportProvider,
                                        ModelMetadataProvider modelProvider,
                                        DataSourceMetadataProvider dataSourceProvider,
                                        EventAutomationMetadataProvider eventProvider,
                                        TableSchemaProvider schemaProvider) {
        AuthorizationService allow = request -> AuthorizationDecision.allow(
                request.subject().policyVersion(), "test", org.fool.framework.common.authz.DataPolicy.unrestricted());
        return new AgentSessionService(
                new InMemoryAgentSessionStore(),
                reportProvider,
                modelProvider,
                dataSourceProvider,
                eventProvider,
                schemaProvider,
                null,
                new AgentOutboundPolicy(),
                allow,
                event -> { },
                Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC));
    }

    private ReportQueryMetadataProvider viewMetadataProvider() {
        return viewId -> ReportQueryMetadataSnapshot.hydrated(
                viewId,
                "OrderList",
                "Order List",
                "OrderList",
                "Order",
                100L,
                "Order",
                "market_order",
                0,
                102L,
                true,
                0,
                List.of(
                        new ReportQueryMetadataColumn(
                                1001L,
                                "Symbol",
                                "Symbol",
                                "symbol",
                                1,
                                false,
                                0,
                                null,
                                null,
                                null,
                                1002L,
                                "symbol",
                                "Symbol",
                                "symbol",
                                1,
                                false),
                        new ReportQueryMetadataColumn(
                                1004L,
                                "Items",
                                "Items",
                                "items",
                                2,
                                false,
                                0,
                                101L,
                                101L,
                                101L,
                                1004L,
                                "items",
                                "Items",
                                null,
                                0,
                                true)),
                List.of(
                        new ReportQueryMetadataOperation(
                                7001L,
                                "删除",
                                7001L,
                                null,
                                2,
                                true,
                                8001L,
                                "操作成功",
                                "操作失败",
                                "确定要删除？")));
    }

    private ModelMetadataProvider modelMetadataProvider() {
        return (modelId, modelName) -> AgentModelMetadataSnapshot.hydrated(
                modelId,
                "Order",
                "Order",
                "Order model",
                0,
                "org.fool.framework.market.Order",
                "market_order",
                false,
                1001L,
                null,
                List.of(
                        new AgentModelPropertyMetadata(
                                1001L,
                                "orderId",
                                "Order ID",
                                null,
                                false,
                                100L,
                                null,
                                null,
                                null,
                                "order_id",
                                1,
                                false,
                                true,
                                null,
                                null,
                                null,
                                null,
                                false),
                        new AgentModelPropertyMetadata(
                                1004L,
                                "items",
                                "Items",
                                101L,
                                true,
                                100L,
                                null,
                                null,
                                null,
                                "items",
                                0,
                                true,
                                false,
                                null,
                                null,
                                null,
                                null,
                                false)),
                List.of(
                        new AgentModelRelationMetadata(
                                0,
                                1004L,
                                "items",
                                100L,
                                1011L,
                                "itemId",
                                101L,
                                "market_order_item",
                                "item_id",
                                "order_id",
                                false)),
                List.of(
                        new AgentModelOperationMetadata(
                                7002L,
                                "保存",
                                null,
                                1,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                1)));
    }

    private DataSourceMetadataProvider dataSourceMetadataProvider() {
        return () -> AgentDataSourceMetadataSnapshot.hydrated(
                List.of(new AgentWorkingDatabaseMetadata(
                        1L,
                        "car_wash",
                        "2026",
                        "car_wash",
                        true,
                        "01",
                        "root",
                        "Docker",
                        "mysql:3306",
                        true,
                        true)),
                List.of(new AgentApplicationRouteMetadata(1L, "fool-service", "01")),
                List.of(new AgentDataSourceRouteMetadata("car_wash", "01")));
    }

    private EventAutomationMetadataProvider eventAutomationMetadataProvider() {
        return () -> AgentEventAutomationMetadataSnapshot.hydrated(
                List.of(new AgentEventDefinitionMetadata(
                        "00000000-0000-0000-0000-000000000100",
                        "[order_state] = 0",
                        "100",
                        "OrderList",
                        "read-order",
                        "Order state matched",
                        60,
                        "100",
                        "Order",
                        "market_order",
                        "order_id",
                        0,
                        "SysModel",
                        0,
                        "IsRunning",
                        1,
                        0,
                        0,
                        0,
                        0)));
    }
}
