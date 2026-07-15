package org.fool.framework.agent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentSessionService {
    private final AgentSessionStore store;
    private final ReportQueryMetadataProvider reportQueryMetadataProvider;
    private final ModelMetadataProvider modelMetadataProvider;
    private final DataSourceMetadataProvider dataSourceMetadataProvider;
    private final EventAutomationMetadataProvider eventAutomationMetadataProvider;
    private final AgentChatProviderService chatProviderService;
    private final Clock clock;
    private final List<AgentCapabilityType> orderedCapabilities;

    @Autowired
    public AgentSessionService(AgentSessionStore store,
                               ReportQueryMetadataProvider reportQueryMetadataProvider,
                               ModelMetadataProvider modelMetadataProvider,
                               DataSourceMetadataProvider dataSourceMetadataProvider,
                               EventAutomationMetadataProvider eventAutomationMetadataProvider,
                               AgentChatProviderService chatProviderService) {
        this(store,
                reportQueryMetadataProvider,
                modelMetadataProvider,
                dataSourceMetadataProvider,
                eventAutomationMetadataProvider,
                chatProviderService,
                Clock.systemUTC());
    }

    AgentSessionService(AgentSessionStore store, Clock clock) {
        this(store,
                ReportQueryMetadataProvider.unavailable(),
                ModelMetadataProvider.unavailable(),
                DataSourceMetadataProvider.unavailable(),
                EventAutomationMetadataProvider.unavailable(),
                clock);
    }

    AgentSessionService(AgentSessionStore store, ReportQueryMetadataProvider reportQueryMetadataProvider, Clock clock) {
        this(store,
                reportQueryMetadataProvider,
                ModelMetadataProvider.unavailable(),
                DataSourceMetadataProvider.unavailable(),
                EventAutomationMetadataProvider.unavailable(),
                clock);
    }

    AgentSessionService(AgentSessionStore store,
                        ReportQueryMetadataProvider reportQueryMetadataProvider,
                        ModelMetadataProvider modelMetadataProvider,
                        Clock clock) {
        this(store,
                reportQueryMetadataProvider,
                modelMetadataProvider,
                DataSourceMetadataProvider.unavailable(),
                EventAutomationMetadataProvider.unavailable(),
                clock);
    }

    AgentSessionService(AgentSessionStore store,
                        ReportQueryMetadataProvider reportQueryMetadataProvider,
                        ModelMetadataProvider modelMetadataProvider,
                        DataSourceMetadataProvider dataSourceMetadataProvider,
                        Clock clock) {
        this(store,
                reportQueryMetadataProvider,
                modelMetadataProvider,
                dataSourceMetadataProvider,
                EventAutomationMetadataProvider.unavailable(),
                clock);
    }

    AgentSessionService(AgentSessionStore store,
                        ReportQueryMetadataProvider reportQueryMetadataProvider,
                        ModelMetadataProvider modelMetadataProvider,
                        DataSourceMetadataProvider dataSourceMetadataProvider,
                        EventAutomationMetadataProvider eventAutomationMetadataProvider,
                        Clock clock) {
        this(store,
                reportQueryMetadataProvider,
                modelMetadataProvider,
                dataSourceMetadataProvider,
                eventAutomationMetadataProvider,
                null,
                clock);
    }

    private AgentSessionService(AgentSessionStore store,
                                ReportQueryMetadataProvider reportQueryMetadataProvider,
                                ModelMetadataProvider modelMetadataProvider,
                                DataSourceMetadataProvider dataSourceMetadataProvider,
                                EventAutomationMetadataProvider eventAutomationMetadataProvider,
                                AgentChatProviderService chatProviderService,
                                Clock clock) {
        this.store = store;
        this.reportQueryMetadataProvider = reportQueryMetadataProvider == null
                ? ReportQueryMetadataProvider.unavailable()
                : reportQueryMetadataProvider;
        this.modelMetadataProvider = modelMetadataProvider == null
                ? ModelMetadataProvider.unavailable()
                : modelMetadataProvider;
        this.dataSourceMetadataProvider = dataSourceMetadataProvider == null
                ? DataSourceMetadataProvider.unavailable()
                : dataSourceMetadataProvider;
        this.eventAutomationMetadataProvider = eventAutomationMetadataProvider == null
                ? EventAutomationMetadataProvider.unavailable()
                : eventAutomationMetadataProvider;
        this.chatProviderService = chatProviderService;
        this.clock = clock;
        this.orderedCapabilities = Arrays.stream(AgentCapabilityType.values())
                .sorted(Comparator.comparingInt(AgentCapabilityType::getOrder))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<AgentCapability> capabilities() {
        return orderedCapabilities.stream().map(AgentCapability::new).toList();
    }

    public AgentSession start(String token, String title) {
        AgentSession session = new AgentSession(
                UUID.randomUUID().toString(),
                normalized(token),
                StringUtils.hasText(title) ? title.trim() : "Agent configuration session",
                orderedCapabilities.get(0),
                now());
        session.addMessage(systemMessage(session.getCurrentCapability(),
                "会话已创建。请先完成“报表/查询”阶段，后续阶段只能按顺序推进。"));
        store.save(session);
        return session;
    }

    public AgentSession get(String sessionId, String token) {
        AgentSession session = find(sessionId);
        assertToken(session, token);
        return session;
    }

    public AgentTurnResult recordUserMessage(String sessionId, String token, AgentCapabilityType capability, String content) {
        return recordUserMessage(sessionId, token, capability, content, Map.of());
    }

    public AgentTurnResult recordUserMessage(String sessionId,
                                             String token,
                                             AgentCapabilityType capability,
                                             String content,
                                             Map<String, Object> context) {
        return recordUserMessage(sessionId, token, capability, content, context, null);
    }

    public AgentTurnResult recordUserMessage(String sessionId,
                                             String token,
                                             AgentCapabilityType capability,
                                             String content,
                                             Map<String, Object> context,
                                             String provider) {
        AgentSession session = get(sessionId, token);
        assertActive(session);
        AgentCapabilityType requested = capability == null ? session.getCurrentCapability() : capability;
        if (requested != session.getCurrentCapability()) {
            throw new IllegalArgumentException("Agent capability must follow the configured order. Current capability is "
                    + session.getCurrentCapability().getId() + ".");
        }
        String normalizedContent = requiredContent(content);
        AgentDraft draft = draftFor(requested, normalizedContent, context == null ? Map.of() : context);
        AgentChatProviderService.Reply generatedReply = chatProviderService == null
                ? AgentChatProviderService.Reply.local(draft.getSummary())
                : chatProviderService.reply(provider, session, requested, normalizedContent, draft);
        session.addMessage(new AgentMessage(UUID.randomUUID().toString(), AgentMessageRole.USER, requested,
                normalizedContent, now()));
        AgentMessage reply = new AgentMessage(UUID.randomUUID().toString(), AgentMessageRole.AGENT, requested,
                generatedReply.getContent(), now());
        session.addMessage(reply);
        store.save(session);
        return new AgentTurnResult(
                session,
                reply,
                draft,
                true,
                generatedReply.getProvider(),
                generatedReply.getModel());
    }

    public AgentSession advance(String sessionId, String token) {
        AgentSession session = get(sessionId, token);
        assertActive(session);
        int index = orderedCapabilities.indexOf(session.getCurrentCapability());
        if (index < 0) {
            throw new IllegalStateException("Unknown current capability.");
        }
        if (index == orderedCapabilities.size() - 1) {
            session.setStatus(AgentSessionStatus.COMPLETED);
            session.addMessage(systemMessage(session.getCurrentCapability(), "agent 配置顺序已完成。"));
            store.save(session);
            return session;
        }
        AgentCapabilityType next = orderedCapabilities.get(index + 1);
        session.setCurrentCapability(next);
        session.addMessage(systemMessage(next, "已进入“" + next.getDisplayName() + "”阶段。"));
        store.save(session);
        return session;
    }

    private AgentSession find(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalArgumentException("sessionId is required.");
        }
        return store.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Agent session not found: " + sessionId));
    }

    private void assertToken(AgentSession session, String token) {
        String normalized = normalized(token);
        if (StringUtils.hasText(session.getToken()) && !Objects.equals(session.getToken(), normalized)) {
            throw new IllegalArgumentException("Token does not match the agent session.");
        }
    }

    private void assertActive(AgentSession session) {
        if (session.getStatus() != AgentSessionStatus.ACTIVE) {
            throw new IllegalStateException("Agent session is already completed.");
        }
    }

    private String normalized(String token) {
        return StringUtils.hasText(token) ? token.trim() : "";
    }

    private String requiredContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("message content is required.");
        }
        return content.trim();
    }

    private AgentMessage systemMessage(AgentCapabilityType capability, String content) {
        return new AgentMessage(UUID.randomUUID().toString(), AgentMessageRole.SYSTEM, capability, content, now());
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private AgentDraft draftFor(AgentCapabilityType capability, String content, Map<String, Object> context) {
        return switch (capability) {
            case REPORT_QUERY -> reportQueryDraft(content, context);
            case FORM_VIEW -> formViewDraft(content, context);
            case MODEL -> modelDraft(content, context);
            case DATA_SOURCE -> dataSourceDraft(content, context);
            case EVENT_AUTOMATION -> eventAutomationDraft(content, context);
        };
    }

    private AgentDraft reportQueryDraft(String content, Map<String, Object> context) {
        Object viewId = firstPresent(context, "viewId", "ViewId", "viewid");
        Long numericViewId = numericViewId(viewId);
        ReportQueryMetadataSnapshot metadata = metadataFor(viewId, numericViewId);
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("ViewId", viewId == null ? "<view-id>" : viewId);
        request.put("CurrentPage", 1);
        request.put("PageSize", 10);
        request.put("FilterExp", null);
        request.put("ReportCols", reportCols(metadata));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reportModelEndpoint", "/api/v1/report/getmkqview");
        payload.put("reportRunEndpoint", "/api/v1/report/makereport");
        payload.put("queryEndpoint", "/api/v1/data/querydata");
        payload.put("metadataSource", "jdbc:fool_sys_view/fool_sys_view_item/fool_sys_model/fool_sys_model_property");
        payload.put("metadataStatus", metadata.getStatus());
        if (StringUtils.hasText(metadata.getReason())) {
            payload.put("metadataReason", metadata.getReason());
        }
        payload.put("view", viewPayload(metadata));
        payload.put("model", modelPayload(metadata));
        payload.put("candidateColumns", candidateColumns(metadata));
        payload.put("draftRequest", request);
        payload.put("sourceRequest", content);

        return new AgentDraft(
                AgentCapabilityType.REPORT_QUERY,
                metadata.isHydrated()
                        ? "已基于 View/Model 元数据生成报表/查询草案：候选列和默认 ReportCols 已绑定当前 View。"
                        : "已生成报表/查询草案框架：需要补齐有效 ViewId 后再读取报表列模型。",
                AgentCapabilityType.REPORT_QUERY.getOwnerModules(),
                "low-read-only",
                payload,
                List.of(
                        "Call /api/v1/report/getmkqview with the selected ViewId and compare the returned Cols with candidateColumns.",
                        "Confirm ReportCols only include View/model-backed columns.",
                        "Run /api/v1/report/makereport with PageSize 10 before saving anything.",
                        "Record the request/response pair as agent evidence."));
    }

    private AgentDraft formViewDraft(String content, Map<String, Object> context) {
        Object viewId = firstPresent(context, "viewId", "ViewId", "viewid");
        Long numericViewId = numericViewId(viewId);
        ReportQueryMetadataSnapshot metadata = metadataFor(viewId, numericViewId);
        List<Map<String, Object>> fields = formFields(metadata);
        List<Map<String, Object>> childCollections = childCollections(metadata);
        List<Map<String, Object>> operations = operations(metadata);

        Map<String, Object> draftView = new LinkedHashMap<>();
        draftView.put("ViewId", metadata.isHydrated() ? metadata.getViewId() : viewId);
        draftView.put("ViewName", metadata.getViewName());
        draftView.put("ViewTitle", metadata.getViewTitle());
        draftView.put("ViewType", metadata.getLegacyViewType());
        draftView.put("DetailViewId", metadata.getDefaultDetailViewId());
        draftView.put("CanEdit", metadata.getViewCanEdit());
        draftView.put("Fields", fields);
        draftView.put("ChildCollections", childCollections);
        draftView.put("Operations", operations);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("listViewEndpoint", "/api/v1/view/getlistview");
        payload.put("readItemViewEndpoint", "/api/v1/view/getreaditemview");
        payload.put("detailEndpoint", "/api/v1/data/querydatadetail");
        payload.put("initNewEndpoint", "/api/v1/data/initnew");
        payload.put("saveEndpoint", "/api/v1/data/save");
        payload.put("runOperationEndpoint", "/api/v1/data/runoperation");
        payload.put("metadataSource", "jdbc:fool_sys_view/fool_sys_view_item/fool_sys_model/fool_sys_model_property/SW_SYS_VIEW/SW_SYS_VIEW_OPERATION/SW_SYS_OPERATIONVIEW");
        payload.put("metadataStatus", metadata.getStatus());
        if (StringUtils.hasText(metadata.getReason())) {
            payload.put("metadataReason", metadata.getReason());
        }
        payload.put("view", viewPayload(metadata));
        payload.put("model", modelPayload(metadata));
        payload.put("fields", fields);
        payload.put("childCollections", childCollections);
        payload.put("operations", operations);
        payload.put("draftView", draftView);
        payload.put("dryRun", "render View metadata without writing business rows or executing operations");
        payload.put("sourceRequest", content);

        return new AgentDraft(
                AgentCapabilityType.FORM_VIEW,
                metadata.isHydrated()
                        ? "已基于 View 元数据生成表单/视图草案：字段、子项和操作按钮已绑定当前 View。"
                        : "已生成表单/视图草案框架：需要补齐有效 ViewId 后再读取 View 字段和操作。",
                AgentCapabilityType.FORM_VIEW.getOwnerModules(),
                "medium-draft-only",
                payload,
                List.of(
                        "Call /api/v1/view/getlistview with the selected ViewId and compare Items/Operations with this draft.",
                        "Call /api/v1/view/getreaditemview for read-only detail rendering before enabling edits.",
                        "Use /api/v1/data/querydatadetail for existing rows or /api/v1/data/initnew for new-row previews.",
                        "Require human approval before /api/v1/data/save or /api/v1/data/runoperation."));
    }

    private AgentDraft modelDraft(String content, Map<String, Object> context) {
        Object modelId = firstPresent(context, "modelId", "ModelId", "modelid");
        Long numericModelId = numericViewId(modelId);
        String modelName = textValue(firstPresent(context, "modelName", "ModelName", "model", "Model", "name", "Name"));
        AgentModelMetadataSnapshot metadata = modelMetadataFor(modelId, numericModelId, modelName, context);
        List<Map<String, Object>> properties = modelProperties(metadata);
        List<Map<String, Object>> relations = modelRelations(metadata);
        List<Map<String, Object>> operations = modelOperations(metadata);

        Map<String, Object> draftModel = new LinkedHashMap<>();
        draftModel.put("ModelId", metadata.getModelId());
        draftModel.put("ModelName", metadata.getModelName());
        draftModel.put("TableName", metadata.getTableName());
        draftModel.put("IdPropertyId", metadata.getIdPropertyId());
        draftModel.put("Properties", properties);
        draftModel.put("Relations", relations);
        draftModel.put("Operations", operations);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("metadataSource", "jdbc:fool_sys_model/fool_sys_model_property/SW_SYS_RELATION/SW_SYS_OPERATION/SW_SYS_COMMANDS");
        payload.put("metadataStatus", metadata.getStatus());
        if (StringUtils.hasText(metadata.getReason())) {
            payload.put("metadataReason", metadata.getReason());
        }
        payload.put("model", modelPayload(metadata));
        payload.put("properties", properties);
        payload.put("relations", relations);
        payload.put("operations", operations);
        payload.put("draftModel", draftModel);
        payload.put("ddlDryRunPlan", ddlDryRunPlan(metadata));
        payload.put("sourceRequest", content);

        return new AgentDraft(
                AgentCapabilityType.MODEL,
                metadata.isHydrated()
                        ? "已基于模型元数据生成模型草案：字段、关系、默认值和操作定义已绑定当前 Model。"
                        : "已生成模型草案框架：需要补齐有效 ModelId 或 ModelName 后再读取模型元数据。",
                AgentCapabilityType.MODEL.getOwnerModules(),
                "high-dry-run-required",
                payload,
                List.of(
                        "Compare model/properties with fool_sys_model and fool_sys_model_property before editing.",
                        "Run a DDL diff against the target table and store the generated SQL as dry-run evidence.",
                        "Confirm relations and operations before changing property ids or command bindings.",
                        "Require human approval before writing model metadata or executing DDL."));
    }

    private AgentDraft dataSourceDraft(String content, Map<String, Object> context) {
        String dataSourceKey = textValue(firstPresent(context,
                "dataSourceKey", "DataSourceKey", "DS_Key", "dsKey", "key"));
        String dbNo = textValue(firstPresent(context, "dbNo", "DBNo", "databaseNo", "DatabaseNo"));
        String appName = textValue(firstPresent(context, "appName", "AppName", "applicationName", "ApplicationName"));
        AgentDataSourceMetadataSnapshot metadata = dataSourceMetadataProvider.load();
        List<Map<String, Object>> workingDatabases = workingDatabases(metadata);
        List<Map<String, Object>> applicationRoutes = applicationRoutes(metadata);
        List<Map<String, Object>> dataSourceRoutes = dataSourceRoutes(metadata);
        Map<String, Object> selectedRoute = selectedDataSourceRoute(metadata, dataSourceKey, dbNo, appName);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("metadataSource", "jdbc:WorkDataBase/DB_App/DB_AppDB/DS_DataSourceSet");
        payload.put("metadataStatus", metadata.getStatus());
        if (StringUtils.hasText(metadata.getReason())) {
            payload.put("metadataReason", metadata.getReason());
        }
        payload.put("workingDatabases", workingDatabases);
        payload.put("applicationRoutes", applicationRoutes);
        payload.put("dataSourceRoutes", dataSourceRoutes);
        payload.put("selectedRoute", selectedRoute);
        payload.put("credentialPolicy", credentialPolicy());
        payload.put("connectivityCheckPlan", connectivityCheckPlan(metadata, selectedRoute));
        payload.put("routingDraft", Map.of(
                "DataSourceKey", dataSourceKey == null ? "<data-source-key>" : dataSourceKey,
                "DBNo", dbNo == null ? selectedRoute.getOrDefault("DbNo", "<db-no>") : dbNo,
                "AppName", appName == null ? "<app-name>" : appName,
                "Mode", "draft-route-only"));
        payload.put("sourceRequest", content);

        return new AgentDraft(
                AgentCapabilityType.DATA_SOURCE,
                metadata.isHydrated()
                        ? "已基于数据源目录生成数据源草案：工作库、应用路由、DS key 和凭据边界已绑定当前配置。"
                        : "已生成数据源草案框架：需要可用 JDBC 元数据后再读取连接目录和路由。",
                AgentCapabilityType.DATA_SOURCE.getOwnerModules(),
                "high-credential-boundary",
                payload,
                List.of(
                        "Compare workingDatabases with WorkDataBase and routes with DB_AppDB/DS_DataSourceSet.",
                        "Do not expose pwd* values or raw connection strings in agent output.",
                        "Resolve credentials server-side and run SELECT 1 as a read-only connectivity check.",
                        "Require permission approval before enabling a route or changing stored credentials."));
    }

    private AgentDraft eventAutomationDraft(String content, Map<String, Object> context) {
        String definitionId = textValue(firstPresent(context,
                "eventDefinitionId", "EventDefinitionId", "definitionId", "DefinitionId", "EVTDEF_ID"));
        AgentEventAutomationMetadataSnapshot metadata = eventAutomationMetadataProvider.load();
        List<Map<String, Object>> definitions = eventDefinitions(metadata);
        Map<String, Object> selectedDefinition = selectedEventDefinition(metadata, definitionId);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("metadataSource", "jdbc:SW_EVT_DEF/SW_EVT_EVENT/SW_SYS_MSG/recipient-relations/fool_sys_model/fool_sys_view");
        payload.put("metadataStatus", metadata.getStatus());
        if (StringUtils.hasText(metadata.getReason())) {
            payload.put("metadataReason", metadata.getReason());
        }
        payload.put("eventDefinitions", definitions);
        payload.put("selectedDefinition", selectedDefinition);
        payload.put("automationDraft", automationDraft(definitionId, selectedDefinition));
        payload.put("dryRunPlan", eventDryRunPlan(selectedDefinition));
        payload.put("auditPlan", eventAuditPlan(selectedDefinition));
        payload.put("sourceRequest", content);

        return new AgentDraft(
                AgentCapabilityType.EVENT_AUTOMATION,
                metadata.isHydrated()
                        ? "已基于事件元数据生成事件/自动化草案：触发条件、接收人关系、幂等和审计计划已绑定当前配置。"
                        : "已生成事件/自动化草案框架：需要可用 JDBC 元数据后再读取事件定义和接收人关系。",
                AgentCapabilityType.EVENT_AUTOMATION.getOwnerModules(),
                "high-dry-run-required",
                payload,
                List.of(
                        "Compare eventDefinitions with SW_EVT_DEF and recipient relation tables before editing.",
                        "Run matched-object dry-run and review the generated object ids before scheduler enablement.",
                        "Use SW_EVT_EVENT(EVT_Defination, EVT_DEF) as the idempotency gate.",
                        "Require human approval before creating events, sending messages, or enabling scheduler mutations."));
    }

    private AgentDraft safeDraft(AgentCapabilityType capability,
                                 String content,
                                 String riskLevel,
                                 Map<String, Object> payload) {
        Map<String, Object> draftPayload = new LinkedHashMap<>(payload);
        draftPayload.put("sourceRequest", content);
        return new AgentDraft(
                capability,
                guidanceFor(capability),
                capability.getOwnerModules(),
                riskLevel,
                draftPayload,
                List.of(
                        "Generate a draft only.",
                        "Produce diff and dry-run evidence before metadata writes.",
                        "Require human approval before advance to runtime mutation."));
    }

    private Object firstPresent(Map<String, Object> context, String... keys) {
        for (String key : keys) {
            if (context.containsKey(key)) {
                return context.get(key);
            }
        }
        return null;
    }

    private ReportQueryMetadataSnapshot metadataFor(Object rawViewId, Long numericViewId) {
        if (rawViewId != null && numericViewId == null) {
            return ReportQueryMetadataSnapshot.unavailable(null, "invalid-view-id", "ViewId must be numeric.");
        }
        return reportQueryMetadataProvider.load(numericViewId);
    }

    private AgentModelMetadataSnapshot modelMetadataFor(Object rawModelId,
                                                        Long numericModelId,
                                                        String modelName,
                                                        Map<String, Object> context) {
        if (rawModelId != null && numericModelId == null) {
            return AgentModelMetadataSnapshot.unavailable(null, modelName, "invalid-model-id", "ModelId must be numeric.");
        }
        Long resolvedModelId = numericModelId;
        String resolvedModelName = modelName;
        if (resolvedModelId == null && !StringUtils.hasText(resolvedModelName)) {
            Object viewId = firstPresent(context, "viewId", "ViewId", "viewid");
            Long numericViewId = numericViewId(viewId);
            ReportQueryMetadataSnapshot viewMetadata = metadataFor(viewId, numericViewId);
            if (viewMetadata.isHydrated()) {
                resolvedModelId = viewMetadata.getModelId();
                resolvedModelName = viewMetadata.getModelName();
            }
        }
        return modelMetadataProvider.load(resolvedModelId, resolvedModelName);
    }

    private Long numericViewId(Object viewId) {
        if (viewId == null) {
            return null;
        }
        if (viewId instanceof Number number) {
            return number.longValue();
        }
        String text = viewId.toString();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String textValue(Object value) {
        if (value == null || !StringUtils.hasText(value.toString())) {
            return null;
        }
        return value.toString().trim();
    }

    private Map<String, Object> viewPayload(ReportQueryMetadataSnapshot metadata) {
        if (!metadata.isHydrated()) {
            return Map.of();
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("ViewId", metadata.getViewId());
        view.put("ViewName", metadata.getViewName());
        view.put("ViewTitle", metadata.getViewTitle());
        view.put("ViewText", metadata.getViewText());
        view.put("ViewModel", metadata.getViewModel());
        view.put("ViewType", metadata.getLegacyViewType());
        view.put("DetailViewId", metadata.getDefaultDetailViewId());
        view.put("CanEdit", metadata.getViewCanEdit());
        view.put("AutoFreshTime", metadata.getAutoFreshInterval());
        return view;
    }

    private Map<String, Object> modelPayload(ReportQueryMetadataSnapshot metadata) {
        if (!metadata.isHydrated()) {
            return Map.of();
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("ModelId", metadata.getModelId());
        model.put("ModelName", metadata.getModelName());
        model.put("TableName", metadata.getModelTable());
        return model;
    }

    private Map<String, Object> modelPayload(AgentModelMetadataSnapshot metadata) {
        if (!metadata.isHydrated()) {
            return Map.of();
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("ModelId", metadata.getModelId());
        model.put("ModelName", metadata.getModelName());
        model.put("ModelText", metadata.getModelText());
        model.put("Remark", metadata.getRemark());
        model.put("ModelType", metadata.getModelType());
        model.put("ClassName", metadata.getClassName());
        model.put("TableName", metadata.getTableName());
        model.put("AutoSysId", metadata.getAutoSysId());
        model.put("IdPropertyId", metadata.getIdPropertyId());
        model.put("DefaultOwnerId", metadata.getDefaultOwnerId());
        return model;
    }

    private List<Map<String, Object>> candidateColumns(ReportQueryMetadataSnapshot metadata) {
        return metadata.getColumns().stream()
                .map(column -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ViewItemId", column.getViewItemId());
                    item.put("ItemName", column.getItemName());
                    item.put("ItemLabel", column.getItemLabel());
                    item.put("ModelProperty", column.getModelProperty());
                    item.put("PropertyId", column.getPropertyId());
                    item.put("PropertyName", column.getPropertyName());
                    item.put("PropertyRemark", column.getPropertyRemark());
                    item.put("DbColumn", column.getDbColumn());
                    item.put("PropertyType", column.getPropertyType());
                    item.put("ShowIndex", column.getShowIndex());
                    item.put("CanEdit", column.getCanEdit());
                    item.put("InputType", column.getInputType());
                    item.put("EditType", column.getEditType());
                    item.put("Width", column.getWidth());
                    item.put("SourceExpression", column.getSourceExpression());
                    item.put("IsCollection", column.getCollection());
                    item.put("Reportable", column.isReportable());
                    item.put("ListViewId", column.getListViewId());
                    item.put("EditViewId", column.getEditViewId());
                    item.put("SelectedViewId", column.getSelectedViewId());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> formFields(ReportQueryMetadataSnapshot metadata) {
        return metadata.getColumns().stream()
                .map(column -> {
                    Map<String, Object> field = new LinkedHashMap<>();
                    field.put("ViewItemId", column.getViewItemId());
                    field.put("Name", column.displayName());
                    field.put("ItemName", column.getItemName());
                    field.put("ItemLabel", column.getItemLabel());
                    field.put("ModelProperty", column.getModelProperty());
                    field.put("PropertyId", column.getPropertyId());
                    field.put("PropertyName", column.getPropertyName());
                    field.put("PropertyRemark", column.getPropertyRemark());
                    field.put("DbColumn", column.getDbColumn());
                    field.put("PropertyType", column.getPropertyType());
                    field.put("InputType", column.getInputType());
                    field.put("EditType", column.getEditType());
                    field.put("CanEdit", column.getCanEdit());
                    field.put("Readonly", !Boolean.TRUE.equals(column.getCanEdit()));
                    field.put("ShowIndex", column.getShowIndex());
                    field.put("Width", column.getWidth());
                    field.put("SourceExpression", column.getSourceExpression());
                    field.put("IsCollection", column.getCollection());
                    field.put("ListViewId", column.getListViewId());
                    field.put("EditViewId", column.getEditViewId());
                    field.put("SelectedViewId", column.getSelectedViewId());
                    field.put("DraftControlType", draftControlType(column));
                    return field;
                })
                .toList();
    }

    private List<Map<String, Object>> childCollections(ReportQueryMetadataSnapshot metadata) {
        return metadata.getColumns().stream()
                .filter(ReportQueryMetadataColumn::isChildCollection)
                .map(column -> {
                    Map<String, Object> child = new LinkedHashMap<>();
                    child.put("ViewItemId", column.getViewItemId());
                    child.put("Name", column.displayName());
                    child.put("ModelProperty", column.getModelProperty());
                    child.put("ListViewId", column.getListViewId());
                    child.put("EditViewId", column.getEditViewId());
                    child.put("SelectedViewId", column.getSelectedViewId());
                    child.put("Readonly", !Boolean.TRUE.equals(column.getCanEdit()));
                    child.put("ShowIndex", column.getShowIndex());
                    return child;
                })
                .toList();
    }

    private List<Map<String, Object>> operations(ReportQueryMetadataSnapshot metadata) {
        return metadata.getOperations().stream()
                .map(operation -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ViewOperationId", operation.getViewOperationId());
                    item.put("OperationId", operation.getOperationId());
                    item.put("OperationViewId", operation.getOperationViewId());
                    item.put("Name", operation.getName());
                    item.put("Text", operation.getName());
                    item.put("Type", operation.operationType());
                    item.put("RequireSelect", Boolean.TRUE.equals(operation.getRequireSelect()));
                    item.put("Location", operation.getLocation() == null ? 0 : operation.getLocation());
                    item.put("ResultViewId", operation.getResultViewId());
                    item.put("ConfirmMessage", operation.getConfirmMessage());
                    item.put("SuccessMessage", operation.getSuccessMessage());
                    item.put("ErrorMessage", operation.getErrorMessage());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> reportCols(ReportQueryMetadataSnapshot metadata) {
        if (!metadata.isHydrated()) {
            return List.of();
        }
        return metadata.reportableColumns().stream()
                .map(column -> {
                    Map<String, Object> reportCol = new LinkedHashMap<>();
                    reportCol.put("ColName", column.displayName());
                    reportCol.put("ColId", column.reportColumnId());
                    reportCol.put("SelectedTypeId", null);
                    reportCol.put("Index", column.getShowIndex());
                    reportCol.put("OrderType", "2");
                    return reportCol;
                })
                .toList();
    }

    private List<Map<String, Object>> modelProperties(AgentModelMetadataSnapshot metadata) {
        return metadata.getProperties().stream()
                .map(property -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("PropertyId", property.getPropertyId());
                    item.put("Name", property.getName());
                    item.put("Remark", property.getRemark());
                    item.put("PropertyModelId", property.getPropertyModelId());
                    item.put("IsCollection", property.getCollection());
                    item.put("OwnerModelId", property.getOwnerId());
                    item.put("Filter", property.getFilter());
                    item.put("Source", property.getSource());
                    item.put("Format", property.getFormat());
                    item.put("DbColumn", property.getDbColumn());
                    item.put("PropertyType", property.getPropertyType());
                    item.put("AllowDbNull", property.getAllowDbNull());
                    item.put("IsCheck", property.getCheck());
                    item.put("IxGroup", property.getIxGroup());
                    item.put("GenerationType", property.getGenerationType());
                    item.put("GenerationExpression", property.getGenerationExpression());
                    item.put("DefaultValue", property.getDefaultValue());
                    item.put("MultiMap", property.getMultiMap());
                    item.put("DdlCheck", StringUtils.hasText(property.getDbColumn()) ? "check-column" : "metadata-only");
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> modelRelations(AgentModelMetadataSnapshot metadata) {
        return metadata.getRelations().stream()
                .map(relation -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("RelationType", relation.getRelationType());
                    item.put("SourcePropertyId", relation.getSourcePropertyId());
                    item.put("SourcePropertyName", relation.getSourcePropertyName());
                    item.put("SourceModelId", relation.getSourceModelId());
                    item.put("TargetPropertyId", relation.getTargetPropertyId());
                    item.put("TargetPropertyName", relation.getTargetPropertyName());
                    item.put("TargetModelId", relation.getTargetModelId());
                    item.put("TableName", relation.getTableName());
                    item.put("SourceColumn", relation.getSourceColumn());
                    item.put("TargetColumn", relation.getTargetColumn());
                    item.put("Nullable", relation.getNullable());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> modelOperations(AgentModelMetadataSnapshot metadata) {
        return metadata.getOperations().stream()
                .map(operation -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("OperationId", operation.getOperationId());
                    item.put("Name", operation.getName());
                    item.put("Filter", operation.getFilter());
                    item.put("BaseType", operation.getBaseType());
                    item.put("ArgModelId", operation.getArgModelId());
                    item.put("ArgFilter", operation.getArgFilter());
                    item.put("InvokeDll", operation.getInvokeDll());
                    item.put("InvokeClass", operation.getInvokeClass());
                    item.put("InvokeMethod", operation.getInvokeMethod());
                    item.put("ReturnModelId", operation.getReturnModelId());
                    item.put("CommandCount", operation.getCommandCount());
                    return item;
                })
                .toList();
    }

    private Map<String, Object> ddlDryRunPlan(AgentModelMetadataSnapshot metadata) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("Mode", "read-only-ddl-diff");
        plan.put("TableName", metadata.getTableName());
        plan.put("IdPropertyId", metadata.getIdPropertyId());
        plan.put("ColumnChecks", ddlColumnChecks(metadata));
        plan.put("RequiredGate", "Generate and review DDL diff before metadata/table writes.");
        return plan;
    }

    private List<Map<String, Object>> ddlColumnChecks(AgentModelMetadataSnapshot metadata) {
        return metadata.getProperties().stream()
                .filter(property -> StringUtils.hasText(property.getDbColumn()))
                .map(property -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("PropertyId", property.getPropertyId());
                    item.put("PropertyName", property.getName());
                    item.put("DbColumn", property.getDbColumn());
                    item.put("PropertyType", property.getPropertyType());
                    item.put("AllowDbNull", property.getAllowDbNull());
                    item.put("DefaultValue", property.getDefaultValue());
                    item.put("GenerationType", property.getGenerationType());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> workingDatabases(AgentDataSourceMetadataSnapshot metadata) {
        return metadata.getWorkingDatabases().stream()
                .map(database -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("DbId", database.getDbId());
                    item.put("DbNo", database.getDbNo());
                    item.put("DbName", database.getDbName());
                    item.put("DbYear", database.getDbYear());
                    item.put("DbSysName", database.getDbSysName());
                    item.put("Active", Boolean.TRUE.equals(database.getActive()));
                    item.put("UserName", database.getUserName());
                    item.put("CompanyName", database.getCompanyName());
                    item.put("ServerIp", database.getServerIp());
                    item.put("Local", Boolean.TRUE.equals(database.getLocal()));
                    item.put("CredentialConfigured", Boolean.TRUE.equals(database.getCredentialConfigured()));
                    item.put("CredentialReference", credentialReference(database.getDbNo()));
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> applicationRoutes(AgentDataSourceMetadataSnapshot metadata) {
        return metadata.getApplicationRoutes().stream()
                .map(route -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("AppId", route.getAppId());
                    item.put("AppName", route.getAppName());
                    item.put("DbNo", route.getDbNo());
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> dataSourceRoutes(AgentDataSourceMetadataSnapshot metadata) {
        return metadata.getDataSourceRoutes().stream()
                .map(route -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("DataSourceKey", route.getDataSourceKey());
                    item.put("DbNo", route.getDbNo());
                    return item;
                })
                .toList();
    }

    private Map<String, Object> selectedDataSourceRoute(AgentDataSourceMetadataSnapshot metadata,
                                                       String dataSourceKey,
                                                       String dbNo,
                                                       String appName) {
        if (StringUtils.hasText(dataSourceKey)) {
            return metadata.getDataSourceRoutes().stream()
                    .filter(route -> equalsIgnoreCase(route.getDataSourceKey(), dataSourceKey))
                    .findFirst()
                    .map(route -> selectedDataSourceKeyRoute(metadata, route))
                    .orElseGet(() -> selectionNotFound("dataSourceKey", dataSourceKey));
        }
        if (StringUtils.hasText(dbNo)) {
            return databaseByDbNo(metadata, dbNo) == null
                    ? selectionNotFound("dbNo", dbNo)
                    : selectedDatabaseRoute(metadata, dbNo, "dbNo");
        }
        if (StringUtils.hasText(appName)) {
            List<AgentApplicationRouteMetadata> routes = metadata.getApplicationRoutes().stream()
                    .filter(route -> equalsIgnoreCase(route.getAppName(), appName))
                    .toList();
            if (routes.isEmpty()) {
                return selectionNotFound("appName", appName);
            }
            Map<String, Object> selected = new LinkedHashMap<>();
            selected.put("SelectionType", "appName");
            selected.put("AppName", appName);
            selected.put("DbNos", routes.stream().map(AgentApplicationRouteMetadata::getDbNo).toList());
            selected.put("WorkingDatabases", routes.stream()
                    .map(route -> databaseByDbNo(metadata, route.getDbNo()))
                    .filter(Objects::nonNull)
                    .map(this::workingDatabase)
                    .toList());
            return selected;
        }
        return Map.of();
    }

    private Map<String, Object> selectedDataSourceKeyRoute(AgentDataSourceMetadataSnapshot metadata,
                                                          AgentDataSourceRouteMetadata route) {
        Map<String, Object> selected = selectedDatabaseRoute(metadata, route.getDbNo(), "dataSourceKey");
        selected.put("DataSourceKey", route.getDataSourceKey());
        return selected;
    }

    private Map<String, Object> selectedDatabaseRoute(AgentDataSourceMetadataSnapshot metadata,
                                                     String dbNo,
                                                     String selectionType) {
        AgentWorkingDatabaseMetadata database = databaseByDbNo(metadata, dbNo);
        Map<String, Object> selected = new LinkedHashMap<>();
        selected.put("SelectionType", selectionType);
        selected.put("DbNo", dbNo);
        selected.put("WorkingDatabase", database == null ? Map.of() : workingDatabase(database));
        selected.put("ApplicationRoutes", metadata.getApplicationRoutes().stream()
                .filter(route -> equalsIgnoreCase(route.getDbNo(), dbNo))
                .map(route -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("AppId", route.getAppId());
                    item.put("AppName", route.getAppName());
                    item.put("DbNo", route.getDbNo());
                    return item;
                })
                .toList());
        return selected;
    }

    private Map<String, Object> selectionNotFound(String key, String value) {
        Map<String, Object> selected = new LinkedHashMap<>();
        selected.put("SelectionType", key);
        selected.put("SelectionValue", value);
        selected.put("Status", "not-found");
        return selected;
    }

    private AgentWorkingDatabaseMetadata databaseByDbNo(AgentDataSourceMetadataSnapshot metadata, String dbNo) {
        return metadata.getWorkingDatabases().stream()
                .filter(database -> equalsIgnoreCase(database.getDbNo(), dbNo))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> workingDatabase(AgentWorkingDatabaseMetadata database) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("DbId", database.getDbId());
        item.put("DbNo", database.getDbNo());
        item.put("DbName", database.getDbName());
        item.put("DbYear", database.getDbYear());
        item.put("DbSysName", database.getDbSysName());
        item.put("Active", Boolean.TRUE.equals(database.getActive()));
        item.put("UserName", database.getUserName());
        item.put("CompanyName", database.getCompanyName());
        item.put("ServerIp", database.getServerIp());
        item.put("Local", Boolean.TRUE.equals(database.getLocal()));
        item.put("CredentialConfigured", Boolean.TRUE.equals(database.getCredentialConfigured()));
        item.put("CredentialReference", credentialReference(database.getDbNo()));
        return item;
    }

    private Map<String, Object> credentialPolicy() {
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("PlaintextSecretsExposed", false);
        policy.put("RawConnectionStringExposed", false);
        policy.put("CredentialColumns", "WorkDataBase.pwd1..pwd5");
        policy.put("SecretHandling", "resolve server-side only; never include pwd* or decrypted password in agent payload");
        policy.put("RequiredGate", "permission boundary and credential owner approval before route activation or credential changes");
        return policy;
    }

    private Map<String, Object> connectivityCheckPlan(AgentDataSourceMetadataSnapshot metadata,
                                                     Map<String, Object> selectedRoute) {
        String dbNo = selectedDbNo(selectedRoute);
        AgentWorkingDatabaseMetadata database = StringUtils.hasText(dbNo) ? databaseByDbNo(metadata, dbNo) : null;
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("Mode", "read-only-connectivity-check");
        plan.put("Query", "SELECT 1");
        plan.put("DbNo", database == null ? "<select DBNo or DataSourceKey>" : database.getDbNo());
        plan.put("DbSysName", database == null ? null : database.getDbSysName());
        plan.put("ServerIp", database == null ? null : database.getServerIp());
        plan.put("CredentialReference", database == null ? "<credential-reference>" : credentialReference(database.getDbNo()));
        plan.put("PlaintextSecretsExposed", false);
        plan.put("RequiredGate", "resolve credentials server-side, enforce permission boundary, and record SELECT 1 evidence");
        return plan;
    }

    private String selectedDbNo(Map<String, Object> selectedRoute) {
        Object value = selectedRoute.get("DbNo");
        if (value != null && StringUtils.hasText(value.toString())) {
            return value.toString();
        }
        return null;
    }

    private String credentialReference(String dbNo) {
        return StringUtils.hasText(dbNo) ? "WorkDataBase.DBNo=" + dbNo + "/pwd*" : "WorkDataBase.pwd*";
    }

    private List<Map<String, Object>> eventDefinitions(AgentEventAutomationMetadataSnapshot metadata) {
        return metadata.getDefinitions().stream()
                .map(this::eventDefinition)
                .toList();
    }

    private Map<String, Object> eventDefinition(AgentEventDefinitionMetadata definition) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("DefinitionId", definition.getDefinitionId());
        item.put("Filter", definition.getFilter());
        item.put("ViewId", definition.getViewId());
        item.put("ViewName", definition.getViewName());
        item.put("OperationId", definition.getOperationId());
        item.put("MessageFormat", definition.getMessageFormat());
        item.put("TimeoutSeconds", definition.getTimeoutSeconds());
        item.put("ModelId", definition.getModelId());
        item.put("ModelName", definition.getModelName());
        item.put("TableName", definition.getTableName());
        item.put("ObjectIdColumn", definition.getObjectIdColumn());
        item.put("ModelRefType", definition.getModelRefType());
        item.put("ModelRefTypeName", definition.getModelRefTypeName());
        item.put("State", definition.getState());
        item.put("StateName", definition.getStateName());
        item.put("Running", Integer.valueOf(0).equals(definition.getState()));
        item.put("RecipientSummary", recipientSummary(definition));
        item.put("ExistingEventCount", count(definition.getExistingEventCount()));
        item.put("QueryPreview", eventQueryPreview(definition));
        return item;
    }

    private Map<String, Object> selectedEventDefinition(AgentEventAutomationMetadataSnapshot metadata,
                                                       String definitionId) {
        if (!StringUtils.hasText(definitionId)) {
            return Map.of();
        }
        return metadata.getDefinitions().stream()
                .filter(definition -> equalsIgnoreCase(definition.getDefinitionId(), definitionId))
                .findFirst()
                .map(this::eventDefinition)
                .orElseGet(() -> selectionNotFound("eventDefinitionId", definitionId));
    }

    private Map<String, Object> recipientSummary(AgentEventDefinitionMetadata definition) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("DirectUsers", count(definition.getNotifyUserCount()));
        summary.put("Roles", count(definition.getNotifyRoleCount()));
        summary.put("Departments", count(definition.getNotifyDepartmentCount()));
        summary.put("Companies", count(definition.getNotifyCompanyCount()));
        summary.put("FallbackWhenEmpty", "All authorized users");
        return summary;
    }

    private Map<String, Object> automationDraft(String definitionId, Map<String, Object> selectedDefinition) {
        Map<String, Object> draft = new LinkedHashMap<>();
        draft.put("DefinitionId", StringUtils.hasText(definitionId)
                ? definitionId
                : selectedDefinition.getOrDefault("DefinitionId", "<event-definition-id>"));
        draft.put("ModelId", selectedDefinition.getOrDefault("ModelId", "<model-id>"));
        draft.put("ViewId", selectedDefinition.getOrDefault("ViewId", "<view-id>"));
        draft.put("OperationId", selectedDefinition.getOrDefault("OperationId", "<operation-id>"));
        draft.put("State", selectedDefinition.getOrDefault("StateName", "<state>"));
        draft.put("Mode", "draft-event-only");
        return draft;
    }

    private Map<String, Object> eventDryRunPlan(Map<String, Object> selectedDefinition) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("Mode", "read-only-matched-object-dry-run");
        plan.put("QueryPreview", selectedDefinition.getOrDefault("QueryPreview", "<resolve event definition first>"));
        plan.put("ObjectIdColumn", selectedDefinition.getOrDefault("ObjectIdColumn", "<object-id-column>"));
        plan.put("IdempotencyCheck", "SW_EVT_EVENT.EVT_Defination + SW_EVT_EVENT.EVT_DEF");
        plan.put("MessagePreview", "resolve recipients and message format without writing SW_EVT_EVENT or SW_SYS_MSG");
        plan.put("SchedulerMutation", false);
        plan.put("RequiredGate", "record matched object ids and recipient preview before enabling event creation");
        return plan;
    }

    private Map<String, Object> eventAuditPlan(Map<String, Object> selectedDefinition) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("EventTable", "SW_EVT_EVENT");
        plan.put("MessageTable", "SW_SYS_MSG");
        plan.put("DefinitionId", selectedDefinition.getOrDefault("DefinitionId", "<event-definition-id>"));
        plan.put("ExistingEventCount", selectedDefinition.getOrDefault("ExistingEventCount", 0));
        plan.put("RequiredEvidence", "definition diff, matched-object dry-run, recipient preview, idempotency check");
        return plan;
    }

    private String eventQueryPreview(AgentEventDefinitionMetadata definition) {
        String tableName = StringUtils.hasText(definition.getTableName())
                ? definition.getTableName()
                : definition.getModelId();
        if (!StringUtils.hasText(tableName)) {
            tableName = "<model-table>";
        }
        String filter = definition.getFilter() == null ? "" : definition.getFilter();
        if (!tableName.trim().startsWith("[")) {
            filter = mysqlBrackets(filter);
        }
        return "SELECT * FROM " + tableName + " WHERE " + filter;
    }

    private String mysqlBrackets(String filter) {
        return filter.replaceAll("\\[([A-Za-z_][A-Za-z0-9_]*)]", "`$1`");
    }

    private int count(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private String draftControlType(ReportQueryMetadataColumn column) {
        if (column.isChildCollection()) {
            return "child-collection";
        }
        if (column.getSelectedViewId() != null || column.getListViewId() != null) {
            return "lookup";
        }
        return Boolean.TRUE.equals(column.getCanEdit()) ? "editable-field" : "readonly-field";
    }

    private String guidanceFor(AgentCapabilityType capability) {
        return switch (capability) {
            case REPORT_QUERY -> "已记录报表/查询需求。下一步应生成只读查询口径、列定义、过滤条件和验证样例。";
            case FORM_VIEW -> "已记录表单/视图需求。下一步应生成 View 元数据、操作按钮、详情分组和只读/编辑状态草案。";
            case MODEL -> "已记录模型需求。下一步应生成字段、关系、默认值、校验和 DDL dry-run 差异。";
            case DATA_SOURCE -> "已记录数据源需求。下一步应生成连接目录、权限边界、凭据引用和路由验证计划。";
            case EVENT_AUTOMATION -> "已记录事件/自动化需求。下一步应生成触发条件、通知对象、幂等策略和审计证据。";
        };
    }
}
