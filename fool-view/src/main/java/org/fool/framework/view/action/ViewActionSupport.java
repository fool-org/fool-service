package org.fool.framework.view.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.DataClassification;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.Property;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.api.ReportController;
import org.fool.framework.view.dto.LegacySaveNewObjRequest;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.dto.MakeReportRequest;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.dto.SaveObjRequest;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.service.DataQueryService;
import org.fool.framework.view.service.ReadAuthorizationEnforcer;
import org.fool.framework.view.service.ViewDataService;
import org.fool.framework.query.CompareFilter;
import org.fool.framework.query.CompareOp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class ViewActionSupport {
    private final DataQueryService dataQueryService;
    private final ViewDataService viewDataService;
    private final ReportController reportController;
    private final DaoService daoService;
    private final JdbcTemplate jdbcTemplate;
    private final ModelDataService modelDataService;
    private final ReadAuthorizationEnforcer authorizationEnforcer;
    private final ObjectMapper objectMapper;

    public ViewActionSupport(DataQueryService dataQueryService,
                             ViewDataService viewDataService,
                             ReportController reportController,
                             DaoService daoService,
                             JdbcTemplate jdbcTemplate,
                             ModelDataService modelDataService,
                             ReadAuthorizationEnforcer authorizationEnforcer,
                             ObjectMapper objectMapper) {
        this.dataQueryService = dataQueryService;
        this.viewDataService = viewDataService;
        this.reportController = reportController;
        this.daoService = daoService;
        this.jdbcTemplate = jdbcTemplate;
        this.modelDataService = modelDataService;
        this.authorizationEnforcer = authorizationEnforcer;
        this.objectMapper = objectMapper.copy()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public SaveObjRequest updateRequest(ControlledActionContext context) {
        SaveObjRequest request = convertRequest(context, SaveObjRequest.class);
        validateSaveObject(context, request.getSaveObj(), true);
        return request;
    }

    public LegacySaveNewObjRequest createRequest(ControlledActionContext context) {
        LegacySaveNewObjRequest request = convertRequest(context, LegacySaveNewObjRequest.class);
        if (StringUtils.hasText(request.getOwnerViewId())) {
            throw denied("BULK_OR_CHILD_WRITE_FORBIDDEN");
        }
        validateSaveObject(context, request.getSaveObj(), false);
        return request;
    }

    public String updateSnapshot(ControlledActionContext context) {
        SaveObjRequest request = updateRequest(context);
        QueryDataDetailResult detail = dataQueryService.queryLegacyViewDataDetail(
                context.resourceId(), request.getSaveObj().getId());
        return hash(detail);
    }

    public String createSnapshot(ControlledActionContext context) {
        LegacySaveNewObjRequest request = createRequest(context);
        View view = requireView(context.resourceId());
        if (modelDataService.getOneData(view.getViewModel(), request.getSaveObj().getId()) != null) {
            throw new ControlledActionException(409, "OBJECT_ALREADY_EXISTS");
        }
        return "new:" + hash(request.getSaveObj());
    }

    public void saveUpdate(ControlledActionContext context) {
        SaveObjRequest request = updateRequest(context);
        requireApprovedSnapshot(context, updateSnapshot(context));
        dataQueryService.saveLegacyObject(request);
        requireCurrentRowScope(context, request.getSaveObj().getId());
    }

    public List<SaveObjRequest> bulkUpdateRequests(ControlledActionContext context) {
        Object value = context.arguments().get("requests");
        if (!(value instanceof List<?> values) || values.size() < 2 || values.size() > 20) {
            throw denied("BULK_ROW_THRESHOLD");
        }
        List<SaveObjRequest> requests = new ArrayList<>();
        Set<String> objectIds = new HashSet<>();
        for (Object item : values) {
            if (!(item instanceof Map<?, ?>)) {
                throw denied("ACTION_ARGUMENTS_INVALID");
            }
            SaveObjRequest request;
            try {
                request = objectMapper.convertValue(item, SaveObjRequest.class);
            } catch (IllegalArgumentException ex) {
                throw denied("ACTION_ARGUMENTS_INVALID");
            }
            validateSaveObject(context, request.getSaveObj(), true);
            if (!objectIds.add(request.getSaveObj().getId())) {
                throw denied("DUPLICATE_OBJECT_ID");
            }
            requests.add(request);
        }
        requests.sort(Comparator.comparing(request -> request.getSaveObj().getId()));
        return List.copyOf(requests);
    }

    public boolean bulkUpdate(ControlledActionContext context) {
        return context.arguments().containsKey("requests");
    }

    public List<String> createFields(ControlledActionContext context) {
        return fields(createRequest(context).getSaveObj());
    }

    public List<String> updateFields(ControlledActionContext context) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (bulkUpdate(context)) {
            bulkUpdateRequests(context).forEach(request -> result.addAll(fields(request.getSaveObj())));
        } else {
            result.addAll(fields(updateRequest(context).getSaveObj()));
        }
        return List.copyOf(result);
    }

    public String bulkUpdateSnapshot(ControlledActionContext context) {
        Map<String, Object> versions = new LinkedHashMap<>();
        for (SaveObjRequest request : bulkUpdateRequests(context)) {
            QueryDataDetailResult detail = dataQueryService.queryLegacyViewDataDetail(
                    context.resourceId(), request.getSaveObj().getId());
            versions.put(request.getSaveObj().getId(), detail);
        }
        return hash(versions);
    }

    public void saveBulkUpdate(ControlledActionContext context) {
        requireApprovedSnapshot(context, bulkUpdateSnapshot(context));
        for (SaveObjRequest request : bulkUpdateRequests(context)) {
            dataQueryService.saveLegacyObject(request);
            requireCurrentRowScope(context, request.getSaveObj().getId());
        }
    }

    public String deleteObjectId(ControlledActionContext context) {
        Object value = context.arguments().get("objectId");
        String objectId = value == null ? "" : String.valueOf(value).trim();
        if (!objectId.matches("[A-Za-z0-9._:-]{1,64}")) {
            throw denied("OBJECT_ID_REQUIRED");
        }
        dataQueryService.queryLegacyViewDataDetail(context.resourceId(), objectId);
        View view = requireView(context.resourceId());
        if (modelDataService.getOneData(view.getViewModel(), objectId) == null) {
            throw new ControlledActionException(404, "OBJECT_NOT_FOUND");
        }
        return objectId;
    }

    public String deleteSnapshot(ControlledActionContext context) {
        String objectId = deleteObjectId(context);
        return hash(dataQueryService.queryLegacyViewDataDetail(context.resourceId(), objectId));
    }

    public void deleteObject(ControlledActionContext context) {
        requireApprovedSnapshot(context, deleteSnapshot(context));
        String objectId = deleteObjectId(context);
        View view = requireView(context.resourceId());
        IDynamicData data = modelDataService.getOneData(view.getViewModel(), objectId);
        if (data == null || !Boolean.TRUE.equals(modelDataService.deleteData(data))) {
            throw new ControlledActionException(409, "DELETE_FAILED");
        }
    }

    public LegacyRunOperationRequest operationRequest(ControlledActionContext context) {
        return operationPlan(context).request();
    }

    public OperationBaseType operationType(ControlledActionContext context) {
        return operationPlan(context).type();
    }

    private OperationPlan operationPlan(ControlledActionContext context) {
        LegacyRunOperationRequest request = convertRequest(context, LegacyRunOperationRequest.class);
        if (request.getOperationId() == null
                || !context.resourceId().equals(request.getOperationId().toString())
                || request.getViewId() == null
                || !StringUtils.hasText(request.getObjectId())) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        View view = viewDataService.getViewData(request.getViewId().toString());
        ViewOperation operation = view == null || view.getOperations() == null ? null
                : view.getOperations().stream()
                .filter(candidate -> candidate.getOperation() != null
                        && request.getOperationId().equals(candidate.getOperation().getId()))
                .findFirst().orElse(null);
        if (operation == null || operation.getOperation() == null) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        OperationBaseType type = operation.getOperation().getBaseOperationType();
        if (!Set.of(OperationBaseType.CREATE, OperationBaseType.UPDATE, OperationBaseType.DELETE).contains(type)) {
            throw new ControlledActionException(403, "CRITICAL_AGENT_EXECUTION_FORBIDDEN");
        }
        dataQueryService.queryLegacyViewDataDetail(request.getViewId().toString(), request.getObjectId());
        return new OperationPlan(request, type);
    }

    public List<String> sensitiveFieldRisk(ControlledActionContext context, Collection<String> fields) {
        return fields != null && fields.stream()
                .map(context.dataPolicy()::classification)
                .anyMatch(level -> level == DataClassification.CONFIDENTIAL
                        || level == DataClassification.RESTRICTED)
                ? List.of("SENSITIVE_FIELD") : List.of();
    }

    public List<String> updateRiskFactors(ControlledActionContext context, Collection<String> fields) {
        LinkedHashSet<String> factors = new LinkedHashSet<>(sensitiveFieldRisk(context, fields));
        Set<String> scopeFields = new LinkedHashSet<>(Set.of(
                "owneruserid", "companyid", "departmentid", "appid", "databaseid"));
        context.dataPolicy().rowRules().forEach(rule -> {
            Object field = rule.filter().get("field");
            if (field != null) {
                scopeFields.add(normalizedField(String.valueOf(field)));
            }
        });
        if (fields != null && fields.stream().map(ViewActionSupport::normalizedField)
                .anyMatch(scopeFields::contains)) {
            factors.add("CROSS_SCOPE");
        }
        return List.copyOf(factors);
    }

    private static List<String> fields(SaveObjRequest.SaveObject object) {
        return object == null || object.getPropertyies() == null ? List.of()
                : object.getPropertyies().stream()
                .filter(java.util.Objects::nonNull)
                .map(SaveObjRequest.SaveKeypair::getKey)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    public String operationSnapshot(ControlledActionContext context) {
        LegacyRunOperationRequest request = operationRequest(context);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("operationId", request.getOperationId());
        snapshot.put("viewId", request.getViewId());
        snapshot.put("objectId", request.getObjectId());
        snapshot.put("object", dataQueryService.queryLegacyViewDataDetail(
                request.getViewId().toString(), request.getObjectId()));
        return hash(snapshot);
    }

    public LegacyRunOperationResult runOperation(ControlledActionContext context) {
        requireApprovedSnapshot(context, operationSnapshot(context));
        LegacyRunOperationResult result = dataQueryService.runLegacyOperation(operationRequest(context));
        if (!result.isSuccess()) {
            throw new ControlledActionException(409, "OPERATION_FAILED");
        }
        return result;
    }

    public void saveCreate(ControlledActionContext context) {
        LegacySaveNewObjRequest request = createRequest(context);
        requireApprovedSnapshot(context, createSnapshot(context));
        dataQueryService.saveLegacyNewObject(request);
        requireCurrentRowScope(context, request.getSaveObj().getId());
    }

    public MakeReportRequest reportRequest(ControlledActionContext context, boolean requireName) {
        MakeReportRequest request = convertRequest(context, MakeReportRequest.class);
        if (request.getViewId() == null || !context.resourceId().equals(request.getViewId().toString())) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        if (StringUtils.hasText(request.getQueryFilter())) {
            throw denied("CLIENT_RAW_FILTER_FORBIDDEN");
        }
        if (CollectionUtils.isEmpty(request.getReportCols()) || request.getReportCols().size() > 100) {
            throw denied("REPORT_COLUMNS_INVALID");
        }
        if (requireName && (!StringUtils.hasText(request.getReportName())
                || request.getReportName().trim().length() > 120)) {
            throw denied("REPORT_NAME_INVALID");
        }
        // Existing report execution reuses the same field and row policy as direct preview.
        MakeReportRequest validation = objectMapper.convertValue(request, MakeReportRequest.class);
        validation.setCurrentPage(1);
        validation.setPageSize(1);
        reportController.makeReport(validation);
        return request;
    }

    public String savedReportSnapshot(ControlledActionContext context) {
        MakeReportRequest request = reportRequest(context, true);
        List<String> definitions = jdbcTemplate.queryForList("""
                SELECT `DEFINITION_JSON` FROM `FOOL_SAVED_REPORT`
                 WHERE `OWNER_USER_ID` = ? AND `APP_ID` = ? AND `DATABASE_ID` = ?
                   AND `VIEW_ID` = ? AND `REPORT_NAME` = ?
                """, String.class, context.subject().userId(), context.subject().appId(),
                context.subject().databaseId(), context.resourceId(), request.getReportName().trim());
        return definitions.isEmpty() ? "absent" : hashJson(definitions.get(0));
    }

    public void saveReport(ControlledActionContext context) {
        requireApprovedSnapshot(context, savedReportSnapshot(context));
        MakeReportRequest request = reportRequest(context, true);
        String definition = json(request);
        jdbcTemplate.update("""
                INSERT INTO `FOOL_SAVED_REPORT`
                  (`SAVED_REPORT_ID`, `OWNER_USER_ID`, `APP_ID`, `DATABASE_ID`, `VIEW_ID`,
                   `REPORT_NAME`, `DEFINITION_JSON`, `VERSION`, `CREATED_AT`, `UPDATED_AT`)
                VALUES (?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
                ON DUPLICATE KEY UPDATE `DEFINITION_JSON` = VALUES(`DEFINITION_JSON`),
                  `VERSION` = `VERSION` + 1, `UPDATED_AT` = CURRENT_TIMESTAMP(6)
                """, java.util.UUID.randomUUID().toString(), context.subject().userId(),
                context.subject().appId(), context.subject().databaseId(), context.resourceId(),
                request.getReportName().trim(), definition);
    }

    public ExportSnapshot exportSnapshot(ControlledActionContext context) {
        MakeReportRequest request = exportRequest(context);
        int max = context.dataPolicy().maxExportRows() == null
                ? 500 : Math.min(context.dataPolicy().maxExportRows(), 500);
        boolean all = Boolean.TRUE.equals(context.arguments().get("all"));
        int requested = request.getPageSize() == null || request.getPageSize() <= 0
                ? max : Math.min(request.getPageSize(), max);
        request.setCurrentPage(all ? 1 : Math.max(1, request.getCurrentPage() == null ? 1 : request.getCurrentPage()));
        request.setPageSize(all ? max : requested);
        Object report = reportController.makeReport(request).getData();
        return new ExportSnapshot(hash(report), report);
    }

    public MakeReportRequest exportRequest(ControlledActionContext context) {
        MakeReportRequest request = reportRequest(context, false);
        View view = requireView(context.resourceId());
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null || model.getProperties() == null) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        for (MakeReportRequest.ReportCol col : request.getReportCols()) {
            String token = StringUtils.hasText(col.getColId()) ? col.getColId() : col.getColName();
            Property property = model.getProperties().stream()
                    .filter(candidate -> Objects.equals(candidate.getName(), token)
                            || Objects.equals(candidate.getColumn(), token)
                            || candidate.getId() != null && Objects.equals(candidate.getId().toString(), token))
                    .findFirst()
                    .orElseThrow(() -> denied("FIELD_NOT_EXPORTABLE"));
            List<String> identities = List.of(
                    property.getName() == null ? "" : property.getName(),
                    property.getColumn() == null ? "" : property.getColumn(),
                    property.getId() == null ? "" : property.getId().toString());
            boolean exportable = identities.stream().filter(StringUtils::hasText)
                    .noneMatch(context.dataPolicy()::denied)
                    && identities.stream().filter(StringUtils::hasText)
                    .anyMatch(context.dataPolicy()::exportable);
            if (!exportable) {
                throw denied("FIELD_NOT_EXPORTABLE");
            }
        }
        return request;
    }

    public String reportDefinitionDiff(ControlledActionContext context) {
        return savedReportSnapshot(context);
    }

    public Map<String, Object> reportDefinitionSummary(ControlledActionContext context) {
        MakeReportRequest request = reportRequest(context, true);
        return Map.of(
                "viewId", context.resourceId(),
                "reportName", request.getReportName().trim(),
                "columns", request.getReportCols().stream()
                        .map(col -> StringUtils.hasText(col.getColId()) ? col.getColId() : col.getColName())
                        .filter(StringUtils::hasText)
                        .toList(),
                "filterConfigured", request.getFilterExp() != null,
                "rawFilterAccepted", false);
    }

    public Map<String, Object> arguments(ControlledActionContext context) {
        return context.arguments();
    }

    public void requireApprovedSnapshot(ControlledActionContext context, String current) {
        Object expected = context.arguments().get("_approvedSnapshotVersion");
        if (!(expected instanceof String value) || !value.equals(current)) {
            throw new ControlledActionException(409, "OBJECT_CHANGED");
        }
    }

    private void requireCurrentRowScope(ControlledActionContext context, String objectId) {
        View view = requireView(context.resourceId());
        Model model = modelDataService.getModel(view.getViewModel());
        if (model == null || model.getIdProperty() == null
                || !StringUtils.hasText(model.getIdProperty().getColumn())) {
            throw new ControlledActionException(403, "POST_WRITE_SCOPE_VIOLATION");
        }
        PageNavigator page = new PageNavigator();
        page.setPageIndex(1);
        page.setPageSize(1);
        PageResult<IDynamicData> rows = modelDataService.getDataListWithPageInfo(
                view.getViewModel(),
                authorizationEnforcer.rowFilter(context.dataPolicy(), model)
                        .and(new CompareFilter(model.getIdProperty().getColumn(), CompareOp.EQUAL, objectId)),
                List.of(model.getIdProperty()), page);
        if (rows == null || CollectionUtils.isEmpty(rows.getItems())) {
            throw new ControlledActionException(403, "POST_WRITE_SCOPE_VIOLATION");
        }
    }

    private static String normalizedField(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private <T> T convertRequest(ControlledActionContext context, Class<T> type) {
        Object value = context.arguments().get("request");
        if (!(value instanceof Map<?, ?>)) {
            throw denied("ACTION_ARGUMENTS_INVALID");
        }
        try {
            return objectMapper.convertValue(value, type);
        } catch (IllegalArgumentException ex) {
            throw denied("ACTION_ARGUMENTS_INVALID");
        }
    }

    private void validateSaveObject(ControlledActionContext context,
                                    SaveObjRequest.SaveObject saveObject,
                                    boolean update) {
        if (saveObject == null || !context.resourceId().equals(saveObject.getViewID())) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        if (!StringUtils.hasText(saveObject.getId())
                || !saveObject.getId().matches("[A-Za-z0-9._:-]{1,64}")) {
            throw denied("OBJECT_ID_REQUIRED");
        }
        if (!CollectionUtils.isEmpty(saveObject.getItemproperties())) {
            throw denied("BULK_OR_CHILD_WRITE_FORBIDDEN");
        }
        View view = requireView(context.resourceId());
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        Map<String, Property> modelFields = new LinkedHashMap<>();
        if (model != null && model.getProperties() != null) {
            for (Property property : model.getProperties()) {
                if (StringUtils.hasText(property.getName()) && !Boolean.TRUE.equals(property.getIsCollection())) {
                    modelFields.put(property.getName(), property);
                }
            }
        }
        if (CollectionUtils.isEmpty(saveObject.getPropertyies())) {
            throw denied("WRITE_FIELDS_REQUIRED");
        }
        Set<String> seen = new HashSet<>();
        for (SaveObjRequest.SaveKeypair pair : saveObject.getPropertyies()) {
            String field = pair == null ? null : pair.getKey();
            Property property = modelFields.get(field);
            if (!StringUtils.hasText(field) || property == null || !seen.add(field)) {
                throw denied("FIELD_NOT_WRITABLE");
            }
            List<String> identities = List.of(
                    property.getName() == null ? "" : property.getName(),
                    property.getColumn() == null ? "" : property.getColumn(),
                    property.getId() == null ? "" : property.getId().toString());
            boolean writable = identities.stream().filter(StringUtils::hasText)
                    .noneMatch(context.dataPolicy()::denied)
                    && identities.stream().filter(StringUtils::hasText)
                    .anyMatch(context.dataPolicy()::writable);
            if (!writable) {
                throw denied("FIELD_NOT_WRITABLE");
            }
        }
    }

    private View requireView(String viewId) {
        View view = daoService.getOneDetailByKey(View.class, viewId);
        if (view == null || !StringUtils.hasText(view.getViewModel())) {
            throw denied("RESOURCE_OUT_OF_SCOPE");
        }
        return view;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw denied("ACTION_ARGUMENTS_INVALID");
        }
    }

    private String hash(Object value) {
        return sha256(json(value));
    }

    private String hashJson(String value) {
        try {
            return hash(objectMapper.readTree(value));
        } catch (JsonProcessingException ex) {
            throw denied("SAVED_REPORT_INVALID");
        }
    }

    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static ControlledActionException denied(String reason) {
        return new ControlledActionException(403, reason);
    }

    public record ExportSnapshot(String version, Object report) {
    }

    private record OperationPlan(LegacyRunOperationRequest request, OperationBaseType type) {
    }
}
