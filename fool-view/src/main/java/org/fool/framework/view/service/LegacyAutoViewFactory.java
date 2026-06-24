package org.fool.framework.view.service;

import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.Operation;
import org.fool.framework.model.model.OperationBaseType;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.fool.framework.view.model.ViewOperation;
import org.fool.framework.view.model.ViewOperationType;
import org.fool.framework.view.model.ViewType;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LegacyAutoViewFactory {
    private final Map<String, View> listViews = new LinkedHashMap<>();
    private final Map<String, View> detailViews = new LinkedHashMap<>();

    public View createDefaultListView(Model model) {
        if (model == null) {
            return null;
        }

        String key = key(model);
        View view = listViews.computeIfAbsent(key, ignored -> {
            View created = new View();
            String title = model.getName() + "列表";
            created.setViewName(title);
            created.setViewTitle(title);
            created.setViewText(title);
            created.setViewModel(model.getName());
            created.setViewModelClass(model.getClassName());
            created.setFilter("");
            created.setViewType(ViewType.ListView);
            created.setDefaultDetailView(createDefaultItemView(model));
            created.setOperations(defaultListOperations(model));
            return created;
        });

        if (view.getListItems() == null || view.getListItems().isEmpty()) {
            view.setListItems(new LinkedList<>());
            for (Property property : nonCollectionProperties(model)) {
                view.getListItems().add(createViewItem(property, true));
            }
        }
        return view;
    }

    public View createDefaultItemView(Model model) {
        if (model == null) {
            return null;
        }

        String key = key(model);
        View view = detailViews.computeIfAbsent(key, ignored -> {
            View created = new View();
            String title = model.getName() + "详细";
            created.setViewName(title);
            created.setViewTitle(title);
            created.setViewText(title);
            created.setViewModel(model.getName());
            created.setViewModelClass(model.getClassName());
            created.setViewType(ViewType.DetailView);
            return created;
        });

        if (view.getListItems() == null || view.getListItems().isEmpty()) {
            view.setListItems(new LinkedList<>());
            for (Property property : safeProperties(model)) {
                view.getListItems().add(createViewItem(property, false));
            }
        }
        return view;
    }

    private List<ViewOperation> defaultListOperations(Model model) {
        List<ViewOperation> operations = new LinkedList<>();
        if (model.getModelType() == ModelType.ENUM) {
            return operations;
        }

        View detailView = createDefaultItemView(model);
        ViewOperation create = new ViewOperation();
        create.setLocation(0);
        create.setName("新建");
        create.setRequireSelect(false);
        create.setType(ViewOperationType.MODAL_DETAIL_VIEW);
        create.setResultView(detailView);
        operations.add(create);

        ViewOperation edit = new ViewOperation();
        edit.setLocation(0);
        edit.setName("编辑");
        edit.setRequireSelect(true);
        edit.setType(ViewOperationType.MODAL_DETAIL_VIEW);
        edit.setResultView(detailView);
        operations.add(edit);

        deleteOperation(model).ifPresent(operation -> {
            ViewOperation delete = new ViewOperation();
            delete.setLocation(2);
            delete.setName("删除");
            delete.setRequireSelect(true);
            delete.setType(ViewOperationType.COMMAND);
            delete.setOperation(operation);
            delete.setConfirmMsg("确定要删除？该操作不可撤消");
            delete.setSuccessMsg("操作成功");
            operations.add(delete);
        });
        return operations;
    }

    private java.util.Optional<Operation> deleteOperation(Model model) {
        return operations(model).stream()
                .filter(operation -> operation.getBaseOperationType() == OperationBaseType.DELETE)
                .findFirst();
    }

    private ViewItem createViewItem(Property property, boolean readOnly) {
        ViewItem item = new ViewItem();
        item.setItemName(displayName(property));
        item.setItemLabel(displayName(property));
        item.setModelProperty(property.getName());
        item.setFormatRegx(Objects.toString(property.getFormat(), ""));
        item.setCanEdit(!readOnly);
        item.setInputType(readOnly ? InputType.READ_ONLY : InputType.TEXT_BOX);
        return item;
    }

    private List<Property> nonCollectionProperties(Model model) {
        return safeProperties(model).stream()
                .filter(property -> !Boolean.TRUE.equals(property.getIsCollection()))
                .toList();
    }

    private List<Property> safeProperties(Model model) {
        return model.getProperties() == null ? List.of() : model.getProperties();
    }

    private List<Operation> operations(Model model) {
        return model.getOperations() == null ? List.of() : model.getOperations();
    }

    private String displayName(Property property) {
        if (property.getRemark() != null && !property.getRemark().isBlank()) {
            return property.getRemark();
        }
        return property.getName();
    }

    private String key(Model model) {
        if (model.getClassName() != null && !model.getClassName().isBlank()) {
            return model.getClassName();
        }
        return model.getName();
    }
}
