package org.fool.framework.view.service;

import org.fool.framework.dao.DaoService;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.dao.PageResult;
import org.fool.framework.dao.QueryAndArgs;
import org.fool.framework.dto.CommonException;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.data.SubItemList;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.service.ModelDataService;
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

    /**
     * 得到视图信息
     *
     * @param viewName
     * @param filter
     * @param pageInfo
     */
    public ListViewResult queryViewDataList(String viewName, Map<String, QueryValue> filter, PageNavigator pageInfo) {
        return queryViewDataList(viewName, filter, pageInfo, null);
    }

    public ListViewResult queryViewDataList(String viewName, Map<String, QueryValue> filter, PageNavigator pageInfo, String keyword) {
        return queryViewDataList(viewName, filter, pageInfo, keyword, null);
    }

    public ListViewResult queryLegacyViewData(String viewId, PageNavigator pageInfo, String queryFilter) {
        return queryViewDataList(viewId, null, pageInfo, null, queryFilter);
    }

    public QueryDataDetailResult queryLegacyViewDataDetail(String viewId, String dataId) {
        View view = viewDataService.getViewData(viewId, null);
        if (view == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "没有查到视图");
        }
        Model model = modelDataService.getModel(view.getViewModel());
        if (model == null) {
            throw new CommonException(ErrorCode.MODEL_NOT_FOUND, "没有查到元数据定义");
        }
        return viewAdapter.getDetailViewResult(view, modelDataService.getOneData(view.getViewModel(), dataId));
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
        View view = daoService.getOneDetailByKey(View.class, request.getViewName());
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
        Property showProperty = showProperty(targetModel);
        InputQueryResult sourceResult = inputQueryFromSourceList(request, view.getViewModel(), model, item, property, showProperty);
        if (sourceResult != null) {
            return sourceResult;
        }
        PageNavigator page = new PageNavigator();
        page.setPageIndex(1);
        page.setPageSize(5);
        PageResult<IDynamicData> pageResult = modelDataService.getDataListWithPageInfo(
                targetModel.getName(),
                likeFilter(showProperty, request.getText()),
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
                || !StringUtils.hasText(sourceKey(sourceExpression, request.isAdded()))) {
            return null;
        }
        List<IDynamicData> owners = sourceOwners(request, modelId, model, sourceExpression);
        if (owners.isEmpty()) {
            return null;
        }
        Object source = owners.get(0).get(sourceKey(sourceExpression, request.isAdded()));
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
        if (request.isAdded()) {
            Model ownerModel = model.getOwner();
            if (!StringUtils.hasText(request.getOwnerId())
                    || ownerModel == null
                    || !sourceExpression.trim().startsWith("#.")
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

    private String sourceKey(String sourceExpression, boolean ownerContext) {
        String expression = sourceExpression == null ? "" : sourceExpression.trim();
        if (ownerContext && expression.startsWith("#.")) {
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
        OperationBaseType operationType = operation.getOperation().getBaseOperationType();
        IDynamicData data = modelDataService.getOneData(view.getViewModel(), request.getObjectId());
        boolean success;
        try {
            if (operationType == OperationBaseType.DELETE) {
                success = Boolean.TRUE.equals(modelDataService.deleteData(data));
            } else if (operationType == OperationBaseType.UPDATE) {
                success = Boolean.TRUE.equals(modelDataService.saveData(data));
            } else {
                return result;
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
        DbMysqlDynamic data = new DbMysqlDynamic(model);
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
                items.add(itemData(property.getPropertyModel(), item, true));
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

    private ListViewResult queryViewDataList(String viewName, Map<String, QueryValue> filter, PageNavigator pageInfo, String keyword, String legacyQueryFilter) {

        View view = daoService.getOneDetailByKey(View.class, viewName);
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
        Property orderProperty = getDefaultOrderProperty(view, model);
        var result = modelDataService.getDataListWithPageInfo(
                view.getViewModel(),
                queryFilter,
                properties,
                pageInfo,
                orderProperty == null ? null : orderColumnExpression(orderProperty),
                orderProperty != null);

        return viewAdapter.getListViewResult(view, result);
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
        Property showProperty = showProperty(targetModel);
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
        if (showProperty == targetModel.getIdProperty()) {
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
        Property showProperty = showProperty(targetModel);
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
        if (showProperty == targetModel.getIdProperty()) {
            return property.getColumn();
        }
        return StringUtils.hasText(showProperty.getColumn())
                ? "`" + property.getName() + "`.`" + showProperty.getColumn() + "`"
                : property.getColumn();
    }

    private Property showProperty(Model model) {
        return model.getShowProperty() == null ? model.getIdProperty() : model.getShowProperty();
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
            var propertyOptional = model.getProperties().stream()
                    .filter(p -> p.getName().equals(item.getModelProperty()))
                    .findFirst();
            if (propertyOptional.isPresent()) {
                return propertyOptional.get();
            }
        }
        return null;
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

}
