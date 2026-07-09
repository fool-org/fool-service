package org.fool.framework.view.service;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.dto.CommonException;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.context.LegacyContextValueService;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.CommandsType;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.OperationCommand;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.service.LegacyAssemblyInvoker;
import org.fool.framework.model.service.ModelDisplayProperties;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.model.service.OperationCommandValueResolver;
import org.fool.framework.query.BetweenFilter;
import org.fool.framework.query.CompareFilter;
import org.fool.framework.query.CompareOp;
import org.fool.framework.query.IQueryFilter;
import org.fool.framework.query.SimpleFilter;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.dto.InputQueryRequest;
import org.fool.framework.view.dto.InputQueryResult;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.dto.LegacySaveNewObjRequest;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.dto.QueryValue;
import org.fool.framework.view.dto.SaveObjRequest;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class DataQueryService {
    @Autowired
    private DaoService daoService;

    @Autowired
    private ViewDataAdapter viewAdapter;
    @Autowired
    private ModelDataService modelDataService;
    @Autowired
    private ViewDataService viewDataService;

    private final OperationCommandValueResolver commandValueResolver = new OperationCommandValueResolver();
    @Autowired(required = false)
    private LegacyContextValueService contextValueService;

    public record QueryOrder(List<Item> items) {
        public QueryOrder {
            items = items == null ? List.of() : items.stream().filter(Objects::nonNull).toList();
        }

        public QueryOrder(String itemToken, boolean descending) {
            this(List.of(new Item(itemToken, descending)));
        }

        public String itemToken() {
            return items.isEmpty() ? null : items.get(0).itemToken();
        }

        public boolean descending() {
            return !items.isEmpty() && items.get(0).descending();
        }

        public record Item(String itemToken, boolean descending) {
        }
    }

    /**
     * 得到视图信息
     *
     * @param viewId
     * @param filter
     * @param pageInfo
     */
    public ListViewResult queryViewDataList(String viewId, Map<String, QueryValue> filter, PageNavigator pageInfo) {
        return queryViewDataList(viewId, filter, pageInfo, null);
    }

    public ListViewResult queryViewDataList(String viewId, Map<String, QueryValue> filter, PageNavigator pageInfo, String keyword) {
        return queryViewDataList(viewId, filter, pageInfo, keyword, null);
    }

    public ListViewResult queryLegacyViewData(String viewId, PageNavigator pageInfo, String queryFilter) {
        return queryLegacyViewData(viewId, pageInfo, queryFilter, null);
    }

    public ListViewResult queryLegacyViewData(String viewId, PageNavigator pageInfo, String queryFilter, String keyword) {
        return queryLegacyViewData(viewId, pageInfo, queryFilter, keyword, null);
    }

    public ListViewResult queryLegacyViewData(
            String viewId,
            PageNavigator pageInfo,
            String queryFilter,
            String keyword,
            QueryOrder order) {
        return queryViewDataList(viewId, null, pageInfo, keyword, queryFilter, order);
    }

    public QueryDataDetailResult queryLegacyViewDataDetail(String viewId, String dataId) {
        return queryLegacyViewDataDetail(viewId, dataId, null);
    }

    public QueryDataDetailResult queryLegacyViewDataDetail(String viewId, String dataId, String idExp) {
        return queryLegacyViewDataDetail(viewId, dataId, idExp, null);
    }

    public QueryDataDetailResult queryLegacyViewDataDetail(String viewId, String dataId, String idExp, String token) {
        View view = viewDataService.getViewData(viewId, token);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = modelDataService.getModel(view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        return viewAdapter.getDetailViewResult(view,
                modelDataService.getOneData(view.getViewModel(), legacyDetailObjectId(dataId, idExp, view, model, token)));
    }

    private String legacyDetailObjectId(String dataId, String idExp, View view, Model model, String token) {
        if (StringUtils.hasText(dataId)) {
            return dataId;
        }
        if (!StringUtils.hasText(idExp)) {
            PageNavigator page = new PageNavigator();
            page.setPageIndex(1);
            page.setPageSize(10);
            PageResult<IDynamicData> result = modelDataService.getDataListWithPageInfo(
                    view.getViewModel(), IQueryFilter.init(),
                    model.getProperties() == null ? List.of() : model.getProperties(),
                    page);
            return result == null || CollectionUtils.isEmpty(result.getItems())
                    ? dataId
                    : result.getItems().get(0).getId();
        }
        String expression = idExp.trim();
        Object resolved = commandValue(null, null, expression, token);
        String resolvedId = resolved == null ? "" : String.valueOf(resolved);
        return StringUtils.hasText(resolvedId) ? resolvedId : dataId;
    }

    public QueryDataDetailResult initLegacyNewObject(String viewId, String parentObjId) {
        View view = viewDataService.getViewData(viewId, null);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = modelDataService.getModel(view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        QueryDataDetailResult result = viewAdapter.getDetailViewResult(view, null);
        if (result.getData() != null && parentObjId != null && !parentObjId.isEmpty()) {
            result.getData().setParentId(parentObjId);
        }
        return result;
    }

    public InputQueryResult inputQuery(InputQueryRequest request) {
        View view = inputQueryView(request);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        attachProperties(view, model);
        ViewItem item = orderedListItems(view).stream()
                .filter(viewItem -> Objects.equals(viewItem.getItemName(), request.getViewItemId())
                        || Objects.equals(viewItem.getModelProperty(), request.getViewItemId()))
                .findFirst()
                .orElseThrow(() -> new CommonException(ErrorCode.VIEW_MODEL_NOT_FOUND, "没有查到视图字段"));
        Property property = item.getProperty();
        Model targetModel = property == null ? null : property.getPropertyModel();
        if (targetModel == null) {
            return new InputQueryResult();
        }
        Property idProperty = targetModel.getIdProperty();
        Property showProperty = ModelDisplayProperties.displayProperty(targetModel);
        InputQueryResult sourceResult = inputQueryFromSourceList(request, view.getViewModel(), model, item, property, showProperty);
        if (sourceResult != null) {
            return sourceResult;
        }
        PageNavigator page = new PageNavigator();
        page.setPageIndex(1);
        page.setPageSize(5);
        IQueryFilter queryFilter = inputQueryFilter(item, showProperty, request.getText());
        PageResult<IDynamicData> pageResult = modelDataService.getDataListWithPageInfo(
                targetModel.getName(),
                queryFilter,
                List.of(idProperty, showProperty),
                page,
                idProperty == null ? null : idProperty.getColumn(),
                true);
        InputQueryResult result = new InputQueryResult();
        if (pageResult.getItems() != null) {
            result.setItems(pageResult.getItems().stream()
                    .map(data -> new InputQueryResult.QueryItem(data.getId(), formatRow(data.get(showProperty.getName()))))
                    .toList());
        }
        return result;
    }

    private View inputQueryView(InputQueryRequest request) {
        if (request.getViewId() == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "ViewId is required");
        }
        return daoService.getOneDetailByKey(View.class, request.getViewId().toString());
    }

    private InputQueryResult inputQueryFromSourceList(
            InputQueryRequest request,
            String modelId,
            Model model,
            ViewItem viewItem,
            Property property,
            Property showProperty) {
        String sourceExpression = sourceExpression(viewItem, property);
        if (!StringUtils.hasText(sourceExpression)
                || showProperty == null
                || !StringUtils.hasText(sourceKey(sourceExpression))) {
            return null;
        }
        List<IDynamicData> owners = sourceOwners(request, modelId, model, sourceExpression);
        if (owners.isEmpty()) {
            return null;
        }
        Object source = owners.get(0).get(sourceKey(sourceExpression));
        if (!(source instanceof Iterable<?> items)) {
            return null;
        }
        String needle = (request.getText() == null ? "" : request.getText().trim()).toUpperCase(Locale.ROOT);
        InputQueryResult result = new InputQueryResult();
        result.setItems(new LinkedList<>());
        for (Object item : items) {
            if (item instanceof IDynamicData data) {
                String text = formatRow(data.get(showProperty.getName()));
                if (text.trim().toUpperCase(Locale.ROOT).contains(needle)) {
                    result.getItems().add(new InputQueryResult.QueryItem(data.getId(), text));
                }
            }
        }
        return result;
    }

    private List<IDynamicData> sourceOwners(
            InputQueryRequest request,
            String modelId,
            Model model,
            String sourceExpression) {
        if (sourceExpression != null && sourceExpression.trim().startsWith("#.")) {
            Model ownerModel = model.getOwner();
            if (!StringUtils.hasText(request.getOwnerId())
                    || ownerModel == null
                    || ownerModel.getIdProperty() == null
                    || !StringUtils.hasText(ownerModel.getIdProperty().getColumn())
                    || ownerModel.getProperties() == null) {
                return List.of();
            }
            return modelDataService.getDataList(
                    ownerModel.getName(),
                    new CompareFilter(ownerModel.getIdProperty().getColumn(), CompareOp.EQUAL, request.getOwnerId()),
                    ownerModel.getProperties());
        }
        if (request.isAdded()) {
            return List.of();
        }
        if (!StringUtils.hasText(request.getObjID())
                || model.getIdProperty() == null
                || !StringUtils.hasText(model.getIdProperty().getColumn())) {
            return List.of();
        }
        return modelDataService.getDataList(
                modelId,
                new CompareFilter(model.getIdProperty().getColumn(), CompareOp.EQUAL, request.getObjID()),
                model.getProperties());
    }

    private String sourceKey(String sourceExpression) {
        String expression = sourceExpression == null ? "" : sourceExpression.trim();
        if (expression.startsWith("#.")) {
            return expression.substring(2);
        }
        if (expression.startsWith(".")) {
            return expression.substring(1);
        }
        return expression;
    }

    private String sourceExpression(ViewItem item, Property property) {
        if (item != null && StringUtils.hasText(item.getSourceExpression())) {
            return item.getSourceExpression();
        }
        return property == null ? null : property.getSource();
    }

    private IQueryFilter inputQueryFilter(ViewItem item, Property showProperty, String text) {
        IQueryFilter filter = likeFilter(showProperty, text);
        if (item == null || item.getSelectedViewId() == null) {
            return filter;
        }
        View selectedView = daoService.getOneDetailByKey(View.class, item.getSelectedViewId().toString());
        if (selectedView == null || !StringUtils.hasText(selectedView.getFilter())) {
            return filter;
        }
        return rawViewFilter(selectedView.getFilter()).and(filter);
    }

    public void saveLegacyObject(SaveObjRequest request) {
        modelDataService.saveData(legacyObjectData(request.getSaveObj()));
    }

    public void saveLegacyNewObject(LegacySaveNewObjRequest request) {
        DbMysqlDynamic data = legacyObjectData(request.getSaveObj());
        if (StringUtils.hasText(request.getOwnerViewId())) {
            View ownerView = daoService.getOneDetailByKey(View.class, request.getOwnerViewId());
            if (ownerView == null) {
                throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
            }
            Model ownerModel = modelDataService.getModel(ownerView.getViewModel());
            Relation relation = ownerRelation(ownerModel, request.getProperty());
            if (relation == null || !StringUtils.hasText(relation.getTargetColumn())) {
                throw new CommonException(ErrorCode.VIEW_MODEL_NOT_FOUND, "没有查到视图字段");
            }
            modelDataService.createData(data, relation.getTargetColumn(), request.getOwnerId());
            return;
        }
        modelDataService.createData(data);
    }

    public LegacyRunOperationResult runLegacyOperation(LegacyRunOperationRequest request) {
        LegacyRunOperationResult result = new LegacyRunOperationResult();
        String viewId = request.getViewId() == null ? null : request.getViewId().toString();
        View view = viewDataService.getViewData(viewId, request.getToken());
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = modelDataService.getModel(view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        ViewOperation operation = findOperation(view, request.getOperationId());
        if (operation == null || operation.getOperation() == null) {
            return result;
        }
        Operation legacyOperation = operation.getOperation();
        OperationBaseType operationType = legacyOperation.getBaseOperationType();
        IDynamicData data = modelDataService.getOneData(view.getViewModel(), request.getObjectId());
        boolean success;
        try {
            if (legacyOperation.getArgModelId() != null) {
                success = executeArgModelOperation(legacyOperation, data, request.getToken());
            } else {
                OperationCommandValues commandValues = applyOperationCommands(
                        legacyOperation, model, data, data, request.getToken());
                if (operationType == OperationBaseType.DELETE) {
                    success = Boolean.TRUE.equals(modelDataService.deleteData(data));
                } else if (operationType == OperationBaseType.UPDATE) {
                    success = Boolean.TRUE.equals(modelDataService.saveData(data));
                } else if (operationType == OperationBaseType.CREATE) {
                    success = Boolean.TRUE.equals(modelDataService.createData(data));
                } else if (operationType == OperationBaseType.ASSEBMLY) {
                    LegacyAssemblyInvoker.invoke(
                            legacyOperation.getInvokeClass(),
                            legacyOperation.getInvokeMethod(),
                            data,
                            commandValues.constructorValues,
                            commandValues.params);
                    success = true;
                } else if (operationType == OperationBaseType.NULL) {
                    success = true;
                } else if (operationType == OperationBaseType.WCF
                        || operationType == OperationBaseType.JSONPOST
                        || operationType == OperationBaseType.JSONGET) {
                    success = true;
                } else {
                    return result;
                }
            }
        } catch (RuntimeException e) {
            result.setSuccess(false);
            result.setReturnMsg((operation.getErrorMsg() == null ? "" : operation.getErrorMsg()) + e);
            return result;
        }
        result.setSuccess(success);
        if (success) {
            result.setReturnMsg(operation.getSuccessMsg() == null ? "" : operation.getSuccessMsg());
        }
        return result;
    }

    private boolean executeArgModelOperation(
            Operation operation,
            IDynamicData sourceData,
            String token) {
        Model targetModel = modelDataService.getModel(operation.getArgModelId().toString());
        if (targetModel == null || !StringUtils.hasText(targetModel.getName())) {
            return false;
        }
        IDynamicData targetData = argModelData(operation, targetModel, sourceData, token);
        if (targetData == null) {
            return false;
        }
        applyOperationCommands(operation, targetModel, targetData, sourceData, token);
        OperationBaseType type = operation.getBaseOperationType();
        if (type == OperationBaseType.CREATE) {
            return Boolean.TRUE.equals(modelDataService.createData(targetData));
        }
        if (type == OperationBaseType.UPDATE) {
            return Boolean.TRUE.equals(modelDataService.saveData(targetData));
        }
        if (type == OperationBaseType.DELETE) {
            return Boolean.TRUE.equals(modelDataService.deleteData(targetData));
        }
        return false;
    }

    private IDynamicData argModelData(
            Operation operation,
            Model targetModel,
            IDynamicData sourceData,
            String token) {
        if (operation.getBaseOperationType() == OperationBaseType.CREATE) {
            return new DbMysqlDynamic(targetModel);
        }
        if (operation.getBaseOperationType() != OperationBaseType.UPDATE
                && operation.getBaseOperationType() != OperationBaseType.DELETE) {
            return null;
        }
        Object targetId = StringUtils.hasText(operation.getArgFilter())
                ? commandValue(null, sourceData, operation.getArgFilter(), token)
                : "";
        return modelDataService.getOneData(targetModel.getName(), targetId == null ? null : String.valueOf(targetId));
    }

    private OperationCommandValues applyOperationCommands(
            Operation operation,
            Model model,
            IDynamicData data,
            IDynamicData valueSource,
            String token) {
        OperationCommandValues values = new OperationCommandValues();
        if (operation == null || CollectionUtils.isEmpty(operation.getCommands()) || data == null) {
            return values;
        }
        operation.getCommands().stream()
                .filter(command -> command != null)
                .sorted(Comparator.comparing(command -> command.getIndex() == null ? 0 : command.getIndex()))
                .forEach(command -> applyOperationCommand(model, data, valueSource, command, values, token));
        return values;
    }

    private void applyOperationCommand(
            Model model,
            IDynamicData data,
            IDynamicData valueSource,
            OperationCommand command,
            OperationCommandValues values,
            String token) {
        if (command.getCommandType() == CommandsType.SET_VALUE) {
            property(model, command.getPropertyId())
                    .ifPresent(property -> data.set(property.getName(),
                            commandValue(property, valueSource, command.getExpression(), token)));
        } else if (command.getCommandType() == CommandsType.FILTER) {
            checkFilterCommand(model, data, command);
        } else if (command.getCommandType() == CommandsType.EXUTE_PROPRTY_MODEL_METHOD) {
            property(model, command.getPropertyId())
                    .ifPresent(property -> invokePropertyModelMethod(data, property, command.getExpression()));
        } else if (command.getCommandType() == CommandsType.EXUTE_LIST_METHOD) {
            property(model, command.getPropertyId())
                    .ifPresent(property -> invokeListMethod(data, property, command.getExpression()));
        } else if (command.getCommandType() == CommandsType.EXUTE_OUT_MODEL_METHOD) {
            IDynamicData result = executeOutModelCommand(model, data, command, token);
            if (result != null) {
                property(model, command.getPropertyId())
                        .ifPresent(property -> data.set(property.getName(),
                                commandValue(property, result, command.getArgExpression(), token)));
            }
        } else if (command.getCommandType() == CommandsType.SET_PARAM_VALUE) {
            values.params.add(commandValue(
                    property(model, command.getPropertyId()).orElse(null), valueSource, command.getExpression(), token));
        } else if (command.getCommandType() == CommandsType.SET_CON_STR_VALUE) {
            values.constructorValues.add(commandValue(
                    property(model, command.getPropertyId()).orElse(null), valueSource, command.getExpression(), token));
        }
    }

    private IDynamicData executeOutModelCommand(
            Model sourceModel,
            IDynamicData sourceData,
            OperationCommand command,
            String token) {
        if (command.getArgModelId() == null) {
            return null;
        }
        Model targetModel = modelDataService.getModel(command.getArgModelId().toString());
        if (targetModel == null || !StringUtils.hasText(targetModel.getName())) {
            return null;
        }
        Object targetId = StringUtils.hasText(command.getArgSourceIdExpression())
                ? commandValue(
                        property(sourceModel, command.getPropertyId()).orElse(null),
                        sourceData,
                        command.getArgSourceIdExpression(),
                        token)
                : "";
        Operation targetOperation = modelOperation(targetModel, command.getExpression());
        if (targetOperation == null) {
            return modelDataService.getOneData(
                    targetModel.getName(), targetId == null ? null : String.valueOf(targetId));
        }
        OperationBaseType type = targetOperation.getBaseOperationType();
        IDynamicData targetData;
        if (type == OperationBaseType.CREATE) {
            targetData = new DbMysqlDynamic(targetModel);
        } else if (type == OperationBaseType.UPDATE || type == OperationBaseType.DELETE) {
            targetData = modelDataService.getOneData(
                    targetModel.getName(), targetId == null ? null : String.valueOf(targetId));
        } else {
            return null;
        }
        if (targetData == null) {
            return null;
        }
        applyOperationCommands(targetOperation, targetModel, targetData, sourceData, token);
        boolean success;
        if (type == OperationBaseType.CREATE) {
            success = Boolean.TRUE.equals(modelDataService.createData(targetData));
        } else if (type == OperationBaseType.UPDATE) {
            success = Boolean.TRUE.equals(modelDataService.saveData(targetData));
        } else {
            success = Boolean.TRUE.equals(modelDataService.deleteData(targetData));
        }
        return success ? targetData : null;
    }

    private Operation modelOperation(Model model, String operationName) {
        if (!StringUtils.hasText(operationName) || CollectionUtils.isEmpty(model.getOperations())) {
            return null;
        }
        String name = operationName.trim().toUpperCase(Locale.ROOT);
        return model.getOperations().stream()
                .filter(operation -> operation != null && StringUtils.hasText(operation.getName()))
                .filter(operation -> operation.getName().trim().toUpperCase(Locale.ROOT).equals(name))
                .findFirst()
                .orElse(null);
    }

    private void invokeListMethod(IDynamicData data, Property property, String methodName) {
        if (data == null || property == null || !StringUtils.hasText(methodName)) {
            return;
        }
        String method = methodName.trim();
        Object target = data.get(property.getName());
        if (target == null) {
            return;
        }
        if (target instanceof IDynamicData dynamicData) {
            dynamicData.invoke(method);
            return;
        }
        try {
            target.getClass().getMethod(method).invoke(target);
        } catch (NoSuchMethodException ignored) {
            // ponytail: no Java list proxy yet; missing list method stays a no-op until a proxy exists.
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            rethrowInvocation(e);
        }
    }

    private void rethrowInvocation(InvocationTargetException e) {
        if (e.getCause() instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new IllegalStateException(e.getCause());
    }

    private void invokePropertyModelMethod(IDynamicData data, Property property, String methodName) {
        if (data == null || property == null || !StringUtils.hasText(methodName)) {
            return;
        }
        String method = methodName.trim();
        Object target = data.get(property.getName());
        if (target instanceof IDynamicData dynamicData) {
            dynamicData.invoke(method);
        } else if (Boolean.TRUE.equals(property.getIsCollection()) && target instanceof Iterable<?> items) {
            items.forEach(item -> {
                if (item instanceof IDynamicData dynamicData) {
                    dynamicData.invoke(method);
                }
            });
        }
    }

    private void checkFilterCommand(Model model, IDynamicData data, OperationCommand command) {
        if (model == null || data == null || !StringUtils.hasText(command.getExpression())) {
            return;
        }
        IQueryFilter filter = rawFilter(command.getExpression());
        Property idProperty = model.getIdProperty();
        String idColumn = idProperty != null && StringUtils.hasText(idProperty.getColumn())
                ? idProperty.getColumn()
                : "SYSID";
        Object idValue = data.getId();
        if (idValue == null) {
            idValue = data.get(idColumn);
        }
        filter = new CompareFilter(idColumn, CompareOp.EQUAL, idValue == null ? "" : String.valueOf(idValue))
                .and(filter);
        List<IDynamicData> matched = modelDataService.getDataList(
                model.getName(), filter, model.getProperties() == null ? List.of() : model.getProperties());
        if (matched.isEmpty()) {
            throw new IllegalStateException(command.getPropertyExpression() == null ? "" : command.getPropertyExpression());
        }
    }

    private java.util.Optional<Property> property(Model model, Long propertyId) {
        if (model == null || CollectionUtils.isEmpty(model.getProperties()) || propertyId == null) {
            return java.util.Optional.empty();
        }
        return model.getProperties().stream()
                .filter(property -> Objects.equals(propertyId, property.getId()))
                .findFirst();
    }

    private Object commandValue(Property property, IDynamicData data, String expression) {
        return commandValueResolver.resolve(property, data, expression, this::businessObjectValue);
    }

    private Object commandValue(Property property, IDynamicData data, String expression, String token) {
        return commandValueResolver.resolve(
                property,
                data,
                expression,
                this::businessObjectValue,
                key -> contextValue(token, key));
    }

    private Object contextValue(String token, String key) {
        return contextValueService == null ? "" : contextValueService.getValue(token, key);
    }

    private Object businessObjectValue(Property property, String value) {
        return property.getPropertyModel() == null
                || !StringUtils.hasText(property.getPropertyModel().getName())
                ? value
                : modelDataService.getOneData(property.getPropertyModel().getName(), value);
    }

    private static class OperationCommandValues {
        private final List<Object> params = new ArrayList<>();
        private final List<Object> constructorValues = new ArrayList<>();
    }

    private ViewOperation findOperation(View view, Long operationId) {
        if (operationId == null || CollectionUtils.isEmpty(view.getOperations())) {
            return null;
        }
        return view.getOperations().stream()
                .filter(operation -> operation != null && operation.getOperation() != null)
                .filter(operation -> Objects.equals(operationId, operation.getOperation().getId()))
                .findFirst()
                .orElse(null);
    }

    private DbMysqlDynamic legacyObjectData(SaveObjRequest.SaveObject saveObj) {
        View view = daoService.getOneDetailByKey(View.class, saveObj.getViewID());
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = modelDataService.getModel(view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        DbMysqlDynamic data = legacySaveData(view.getViewModel(), saveObj, model);
        Property idProperty = model.getIdProperty();
        if (idProperty != null && idProperty.getName() != null) {
            data.set(idProperty.getName(), saveObj.getId());
        }
        for (SaveObjRequest.SaveKeypair pair : saveObj.getPropertyies()) {
            data.set(pair.getKey(), pair.getValue());
        }
        for (SaveObjRequest.ItemProperty itemProperty : saveObj.getItemproperties()) {
            Property property = collectionProperty(model, itemProperty.getKey());
            if (property == null || property.getPropertyModel() == null) {
                continue;
            }
            SubItemList<DbMysqlDynamic> items = new SubItemList<>();
            for (SaveObjRequest.Item item : itemProperty.getItems()) {
                DbMysqlDynamic updated = itemData(property.getPropertyModel(), item, true);
                items.add(updated);
                items.getAddedList().remove(updated);
                items.getUpdatedList().add(updated);
            }
            for (SaveObjRequest.Item item : itemProperty.getAddedItems()) {
                items.add(itemData(property.getPropertyModel(), item, item.isExist()));
            }
            for (SaveObjRequest.Item item : itemProperty.getDelteItems()) {
                DbMysqlDynamic deleted = itemData(property.getPropertyModel(), item, true);
                items.add(deleted);
                items.remove(deleted);
            }
            data.set(itemProperty.getKey(), items);
        }
        return data;
    }

    private DbMysqlDynamic legacySaveData(String modelId, SaveObjRequest.SaveObject saveObj, Model model) {
        if (StringUtils.hasText(saveObj.getId())) {
            IDynamicData existing = modelDataService.getOneData(modelId, saveObj.getId());
            if (existing instanceof DbMysqlDynamic dynamicData) {
                return dynamicData;
            }
        }
        return new DbMysqlDynamic(model);
    }

    private Relation ownerRelation(Model model, String propertyName) {
        if (model == null || model.getRelations() == null || !StringUtils.hasText(propertyName)) {
            return null;
        }
        return model.getRelations().stream()
                .filter(relation -> relation.getProperty() != null)
                .filter(relation -> Objects.equals(relation.getProperty().getName(), propertyName))
                .findFirst()
                .orElse(null);
    }

    private Property collectionProperty(Model model, String name) {
        if (model.getProperties() == null) {
            return null;
        }
        return model.getProperties().stream()
                .filter(property -> Boolean.TRUE.equals(property.getIsCollection()))
                .filter(property -> Objects.equals(property.getName(), name))
                .findFirst()
                .orElse(null);
    }

    private DbMysqlDynamic itemData(Model model, SaveObjRequest.Item item, boolean keepId) {
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        Property idProperty = model.getIdProperty();
        if (keepId && idProperty != null && idProperty.getName() != null) {
            data.set(idProperty.getName(), item.getItemId());
        }
        for (SaveObjRequest.SaveKeypair pair : item.getPropertyies()) {
            data.set(pair.getKey(), pair.getValue());
        }
        return data;
    }

    private ListViewResult queryViewDataList(String viewId, Map<String, QueryValue> filter, PageNavigator pageInfo, String keyword, String legacyQueryFilter) {
        return queryViewDataList(viewId, filter, pageInfo, keyword, legacyQueryFilter, null);
    }

    private ListViewResult queryViewDataList(
            String viewId,
            Map<String, QueryValue> filter,
            PageNavigator pageInfo,
            String keyword,
            String legacyQueryFilter,
            QueryOrder order) {

        View view = daoService.getOneDetailByKey(View.class, ViewDataService.requireViewId(viewId));
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = daoService.getOneDetailByKey(Model.class, view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        var properties = getViewProperies(view, model);
        attachProperties(view, model);
        IQueryFilter queryFilter = generateFilter(model, view, filter, keyword, legacyQueryFilter);
        OrderSelection orderSelection = orderSelection(view, model, order);
        var result = queryDataList(
                view.getViewModel(),
                queryFilter,
                properties,
                pageInfo,
                orderSelection.columns());

        return viewAdapter.getListViewResult(view, result);
    }

    private PageResult<IDynamicData> queryDataList(
            String viewModel,
            IQueryFilter queryFilter,
            List<Property> properties,
            PageNavigator pageInfo,
            List<ModelDataService.OrderColumn> orderColumns) {
        if (orderColumns == null || orderColumns.isEmpty()) {
            return modelDataService.getDataListWithPageInfo(viewModel, queryFilter, properties, pageInfo, null, false);
        }
        if (orderColumns.size() == 1) {
            ModelDataService.OrderColumn orderColumn = orderColumns.get(0);
            return modelDataService.getDataListWithPageInfo(
                    viewModel, queryFilter, properties, pageInfo, orderColumn.column(), orderColumn.descending());
        }
        return modelDataService.getDataListWithPageInfo(viewModel, queryFilter, properties, pageInfo, orderColumns);
    }

    /**
     * 生成简单查询表达式
     *
     * @param model
     * @param filter
     * @return
     */
    private IQueryFilter generateFilter(Model model, View view, Map<String, QueryValue> filter, String keyword, String legacyQueryFilter) {
        IQueryFilter queryFilter = rawViewFilter(view.getFilter());
        queryFilter = appendRawFilter(queryFilter, legacyQueryFilter);
        queryFilter = appendKeywordFilter(queryFilter, model, view, keyword);
        var properties = model.getProperties();
        if (filter != null) {
            for (var key : filter.keySet()
            ) {
                var value = filter.get(key);
                if (properties.stream().filter(p -> p.getName().equals(key)).count() > 0) {
                    if (!StringUtils.isEmpty(value.getValue())) {
                        /**
                         * 如果传了一个值，就是相等
                         */
                        queryFilter = queryFilter.and(new CompareFilter(properties.stream().filter(p -> p.getName().equals(key)).findFirst().get().getColumn(), CompareOp.EQUAL, filter.get(key).getValue()));
                    } else if ((!CollectionUtils.isEmpty(value.getValues())) && value.getValues().size() == 2) {
                        /**
                         * 如果传了两值就是between
                         */
                        queryFilter = queryFilter.and(new BetweenFilter(properties.stream().filter(p -> p.getName().equals(key)).findFirst().get().getColumn(), value.getValues().get(0), value.getValues().get(1)));
                    }
                }
            }
        }
        return queryFilter;
    }

    private IQueryFilter appendRawFilter(IQueryFilter queryFilter, String rawFilter) {
        if (!StringUtils.hasText(rawFilter)) {
            return queryFilter;
        }
        return queryFilter.and(rawFilter(rawFilter));
    }

    private IQueryFilter appendKeywordFilter(IQueryFilter queryFilter, Model model, View view, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return queryFilter;
        }
        var pattern = "%" + keyword.trim() + "%";
        var columnExpressions = orderedListItems(view).stream()
                .filter(this::readOnlyItem)
                .map(ViewItem::getModelProperty)
                .map(name -> model.getProperties().stream()
                        .filter(property -> property.getName().equals(name))
                        .findFirst()
                        .orElse(null))
                .filter(property -> property != null
                        && !Boolean.TRUE.equals(property.getIsCollection()))
                .map(this::keywordColumnExpression)
                .filter(StringUtils::hasText)
                .toList();
        if (columnExpressions.isEmpty()) {
            return queryFilter;
        }
        return queryFilter.and(new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs queryAndArgs = new QueryAndArgs();
                queryAndArgs.setSql(columnExpressions.stream()
                        .map(column -> column + " LIKE ?")
                        .reduce((left, right) -> left + " OR " + right)
                        .orElse(" 1=1 "));
                queryAndArgs.setArgs(columnExpressions.stream().map(column -> pattern).toArray());
                return queryAndArgs;
            }
        });
    }

    private String keywordColumnExpression(Property property) {
        if (!PropertyType.BusinessObject.equals(property.getPropertyType())) {
            return StringUtils.hasText(property.getColumn()) ? "`" + property.getColumn() + "`" : null;
        }
        Model targetModel = property.getPropertyModel();
        if (targetModel == null) {
            return null;
        }
        Property showProperty = ModelDisplayProperties.displayProperty(targetModel);
        if (showProperty == null) {
            return null;
        }
        if (Boolean.TRUE.equals(property.getMultiMap())) {
            return safeDbMaps(property).stream()
                    .filter(map -> map != null)
                    .filter(map -> showProperty.getName().equals(map.getPropertyName()))
                    .map(MultiDbMap::getColumnName)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .map(column -> "`" + column + "`")
                    .orElse(null);
        }
        if (ModelDisplayProperties.sameProperty(showProperty, targetModel.getIdProperty())) {
            return StringUtils.hasText(property.getColumn()) ? "`" + property.getColumn() + "`" : null;
        }
        return StringUtils.hasText(showProperty.getColumn())
                ? "`" + property.getName() + "`.`" + showProperty.getColumn() + "`"
                : null;
    }

    private String orderColumnExpression(Property property) {
        if (!PropertyType.BusinessObject.equals(property.getPropertyType())) {
            return property.getColumn();
        }
        Model targetModel = property.getPropertyModel();
        if (targetModel == null) {
            return property.getColumn();
        }
        Property showProperty = ModelDisplayProperties.displayProperty(targetModel);
        if (showProperty == null) {
            return property.getColumn();
        }
        if (Boolean.TRUE.equals(property.getMultiMap())) {
            return safeDbMaps(property).stream()
                    .filter(map -> map != null)
                    .filter(map -> showProperty.getName().equals(map.getPropertyName()))
                    .map(MultiDbMap::getColumnName)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(property.getColumn());
        }
        if (ModelDisplayProperties.sameProperty(showProperty, targetModel.getIdProperty())) {
            return property.getColumn();
        }
        return StringUtils.hasText(showProperty.getColumn())
                ? "`" + property.getName() + "`.`" + showProperty.getColumn() + "`"
                : property.getColumn();
    }

    private String formatRow(Object value) {
        return value == null ? "" : value.toString();
    }

    private IQueryFilter likeFilter(Property property, String text) {
        return new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs queryAndArgs = new QueryAndArgs();
                queryAndArgs.setSql("`" + property.getColumn() + "` LIKE ?");
                queryAndArgs.setArgs(new Object[]{"%" + (text == null ? "" : text) + "%"});
                return queryAndArgs;
            }
        };
    }

    private List<MultiDbMap> safeDbMaps(Property property) {
        return property.getDbMaps() == null ? List.of() : property.getDbMaps();
    }

    private boolean readOnlyItem(ViewItem item) {
        return item.getInputType() == InputType.READ_ONLY || !item.isCanEdit();
    }

    private IQueryFilter rawViewFilter(String viewFilter) {
        if (!StringUtils.hasText(viewFilter)) {
            return IQueryFilter.init();
        }
        return rawFilter(viewFilter);
    }

    private IQueryFilter rawFilter(String filter) {
        return new SimpleFilter() {
            @Override
            public QueryAndArgs generateSql() {
                QueryAndArgs queryAndArgs = new QueryAndArgs();
                queryAndArgs.setSql(filter);
                queryAndArgs.setArgs(new Object[]{});
                return queryAndArgs;
            }
        };
    }

    private List<Property> getViewProperies(View view, Model model) {
        List<Property> result = new LinkedList<>();
        for (var item : orderedListItems(view)) {
            var propertyOptional = model.getProperties().stream().filter(p -> p.getName().equals(item.getModelProperty())).findFirst();
            if (propertyOptional.isPresent()) {
                result.add(propertyOptional.get());
            }
        }
        return result;
    }

    private void attachProperties(View view, Model model) {
        if (CollectionUtils.isEmpty(view.getListItems()) || CollectionUtils.isEmpty(model.getProperties())) {
            return;
        }
        view.getListItems().forEach(item -> model.getProperties().stream()
                .filter(property -> item.getModelProperty() != null
                        && item.getModelProperty().equals(property.getName()))
                .findFirst()
                .ifPresent(item::setProperty));
    }

    private Property getDefaultOrderProperty(View view, Model model) {
        for (var item : orderedListItems(view)) {
            Property property = propertyForItem(model, item);
            if (property != null) {
                return property;
            }
        }
        return null;
    }

    private OrderSelection orderSelection(View view, Model model, QueryOrder order) {
        List<ModelDataService.OrderColumn> columns = orderColumns(view, model, order);
        if (!columns.isEmpty()) {
            return new OrderSelection(columns);
        }
        Property defaultProperty = getDefaultOrderProperty(view, model);
        return new OrderSelection(defaultProperty == null
                ? List.of()
                : List.of(new ModelDataService.OrderColumn(orderColumnExpression(defaultProperty), true)));
    }

    private List<ModelDataService.OrderColumn> orderColumns(View view, Model model, QueryOrder order) {
        if (order == null || order.items().isEmpty()) {
            return List.of();
        }
        return order.items().stream()
                .map(item -> orderColumn(view, model, item))
                .filter(Objects::nonNull)
                .toList();
    }

    private ModelDataService.OrderColumn orderColumn(View view, Model model, QueryOrder.Item item) {
        if (item == null || !StringUtils.hasText(item.itemToken())) {
            return null;
        }
        Property property = propertyByViewToken(view, model, item.itemToken());
        if (property == null) {
            return null;
        }
        String column = orderColumnExpression(property);
        return StringUtils.hasText(column) ? new ModelDataService.OrderColumn(column, item.descending()) : null;
    }

    private Property propertyByViewToken(View view, Model model, String token) {
        if (model == null || CollectionUtils.isEmpty(model.getProperties()) || !StringUtils.hasText(token)) {
            return null;
        }
        String trimmed = token.trim();
        for (ViewItem item : orderedListItems(view)) {
            Property property = propertyForItem(model, item);
            if (property != null && matchesViewToken(item, property, trimmed)) {
                return property;
            }
        }
        return null;
    }

    private Property propertyForItem(Model model, ViewItem item) {
        if (item.getProperty() != null) {
            return item.getProperty();
        }
        if (!StringUtils.hasText(item.getModelProperty()) || CollectionUtils.isEmpty(model.getProperties())) {
            return null;
        }
        return model.getProperties().stream()
                .filter(property -> item.getModelProperty().equals(property.getName()))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesViewToken(ViewItem item, Property property, String token) {
        return Objects.equals(item.getItemName(), token)
                || Objects.equals(item.getItemLabel(), token)
                || Objects.equals(item.getModelProperty(), token)
                || (item.getId() != null && Objects.equals(item.getId().toString(), token))
                || matchesProperty(property, token);
    }

    private boolean matchesProperty(Property property, String token) {
        return Objects.equals(property.getName(), token)
                || Objects.equals(property.getRemark(), token)
                || Objects.equals(property.getColumn(), token)
                || (property.getId() != null && Objects.equals(property.getId().toString(), token));
    }

    private List<ViewItem> orderedListItems(View view) {
        if (CollectionUtils.isEmpty(view.getListItems())) {
            return List.of();
        }
        return view.getListItems().stream()
                .sorted(Comparator.comparingInt(this::safeShowIndex))
                .toList();
    }

    private int safeShowIndex(ViewItem item) {
        return item.getShowIndex() == null ? 0 : item.getShowIndex();
    }

    private record OrderSelection(List<ModelDataService.OrderColumn> columns) {
    }

}
