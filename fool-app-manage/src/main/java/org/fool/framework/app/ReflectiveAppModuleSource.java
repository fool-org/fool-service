package org.fool.framework.app;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.EncryptType;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Key;
import org.fool.framework.common.annotation.MultiType;
import org.fool.framework.common.annotation.ReferToProperty;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.common.data.ObjectWithSubItem;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.ModelType;
import org.fool.framework.model.model.MultiDbMap;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectiveAppModuleSource implements AppModuleSource {
    private final StaticAppModuleSource delegate;
    private final Map<Class<?>, Model> modelsByType = new LinkedHashMap<>();
    private final Map<Model, Class<?>> typesByModel = new IdentityHashMap<>();
    private final Map<Property, Field> fieldsByProperty = new IdentityHashMap<>();

    public ReflectiveAppModuleSource(
            String moduleName,
            String remark,
            String version,
            List<Class<?>> modelTypes) {
        AppModuleDefinition module = AppModuleDefinition.legacy(
                moduleName,
                remark,
                version,
                buildModels(modelTypes));
        this.delegate = new StaticAppModuleSource(List.of(module));
    }

    public ReflectiveAppModuleSource(
            String moduleName,
            String remark,
            String version,
            String packageName,
            ClassLoader classLoader) {
        this.delegate = new StaticAppModuleSource(buildPackageModules(
                moduleName,
                remark,
                version,
                packageName,
                List.of(),
                classLoader));
    }

    public ReflectiveAppModuleSource(
            String moduleName,
            String remark,
            String version,
            String packageName,
            List<String> dependencyPackageNames,
            ClassLoader classLoader) {
        this.delegate = new StaticAppModuleSource(buildPackageModules(
                moduleName,
                remark,
                version,
                packageName,
                dependencyPackageNames,
                classLoader));
    }

    public ReflectiveAppModuleSource(
            String moduleName,
            String remark,
            String version,
            String packageName,
            List<String> dependencyPackageNames) {
        this(moduleName, remark, version, packageName, dependencyPackageNames, Thread.currentThread().getContextClassLoader());
    }

    public ReflectiveAppModuleSource(
            String moduleName,
            String remark,
            String version,
            String packageName) {
        this(moduleName, remark, version, packageName, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public List<AppModuleDefinition> getModules() {
        return delegate.getModules();
    }

    @Override
    public List<Model> getModels(AppModuleDefinition module) {
        return delegate.getModels(module);
    }

    private List<Model> buildModels(List<Class<?>> modelTypes) {
        if (modelTypes != null) {
            for (Class<?> modelType : modelTypes) {
                modelFor(modelType);
            }
        }
        wireRelations();
        return new ArrayList<>(modelsByType.values());
    }

    private List<AppModuleDefinition> buildPackageModules(
            String moduleName,
            String remark,
            String version,
            String packageName,
            List<String> dependencyPackageNames,
            ClassLoader classLoader) {
        List<Class<?>> rootTypes = scanPackage(packageName, classLoader);
        Set<Class<?>> rootTypeSet = Collections.newSetFromMap(new IdentityHashMap<>());
        rootTypeSet.addAll(rootTypes);
        for (Class<?> modelType : rootTypes) {
            modelFor(modelType);
        }
        for (String dependencyPackageName : safePackageNames(dependencyPackageNames)) {
            for (Class<?> modelType : scanPackage(dependencyPackageName, classLoader)) {
                modelFor(modelType);
            }
        }
        wireRelations();

        List<Model> rootModels = new ArrayList<>();
        Map<String, List<Model>> referencedModelsByPackage = new TreeMap<>();
        for (Map.Entry<Class<?>, Model> entry : modelsByType.entrySet()) {
            if (rootTypeSet.contains(entry.getKey())) {
                rootModels.add(entry.getValue());
            } else {
                referencedModelsByPackage
                        .computeIfAbsent(entry.getKey().getPackageName(), ignored -> new ArrayList<>())
                        .add(entry.getValue());
            }
        }

        List<AppModuleDefinition> modules = new ArrayList<>();
        for (Map.Entry<String, List<Model>> entry : referencedModelsByPackage.entrySet()) {
            modules.add(AppModuleDefinition.legacy(entry.getKey(), entry.getKey(), version, entry.getValue()));
        }
        modules.add(AppModuleDefinition.legacy(moduleName, remark, version, rootModels));
        wireModuleDependencies(modules);
        addDeclaredPackageDependencies(modules, moduleName, safePackageNames(dependencyPackageNames));
        return modules;
    }

    private List<String> safePackageNames(List<String> packageNames) {
        if (packageNames == null) {
            return List.of();
        }
        return packageNames.stream()
                .filter(packageName -> packageName != null && !packageName.isBlank())
                .distinct()
                .toList();
    }

    private void addDeclaredPackageDependencies(
            List<AppModuleDefinition> modules,
            String moduleName,
            List<String> dependencyPackageNames) {
        if (dependencyPackageNames.isEmpty()) {
            return;
        }
        AppModuleDefinition rootModule = null;
        Map<String, AppModuleDefinition> moduleByName = new LinkedHashMap<>();
        for (AppModuleDefinition module : modules) {
            moduleByName.put(module.getName(), module);
            if (module.getName().equals(moduleName)) {
                rootModule = module;
            }
        }
        if (rootModule == null) {
            return;
        }
        List<AppModuleDefinition> dependencies = new ArrayList<>(rootModule.getDependencies());
        for (String packageName : dependencyPackageNames) {
            AppModuleDefinition dependency = moduleByName.get(packageName);
            if (dependency != null
                    && dependency != rootModule
                    && dependencies.stream().noneMatch(existing -> existing == dependency)) {
                dependencies.add(dependency);
            }
        }
        rootModule.setDependencies(dependencies);
    }

    private void wireModuleDependencies(List<AppModuleDefinition> modules) {
        Map<Model, AppModuleDefinition> moduleByModel = new IdentityHashMap<>();
        for (AppModuleDefinition module : modules) {
            for (Model model : safeModels(module)) {
                moduleByModel.put(model, module);
            }
        }
        for (AppModuleDefinition module : modules) {
            List<AppModuleDefinition> dependencies = new ArrayList<>();
            for (Model model : safeModels(module)) {
                addModuleDependency(module, model.getBaseModel(), moduleByModel, dependencies);
                for (Property property : safeProperties(model)) {
                    addModuleDependency(module, property.getPropertyModel(), moduleByModel, dependencies);
                }
            }
            module.setDependencies(dependencies);
        }
    }

    private List<Model> safeModels(AppModuleDefinition module) {
        return module.getModels() == null ? List.of() : module.getModels();
    }

    private void addModuleDependency(
            AppModuleDefinition ownerModule,
            Model referencedModel,
            Map<Model, AppModuleDefinition> moduleByModel,
            List<AppModuleDefinition> dependencies) {
        if (referencedModel == null) {
            return;
        }
        AppModuleDefinition dependency = moduleByModel.get(referencedModel);
        if (dependency != null
                && dependency != ownerModule
                && dependencies.stream().noneMatch(existing -> existing == dependency)) {
            dependencies.add(dependency);
        }
    }

    private Model modelFor(Class<?> type) {
        if (type == null || Object.class.equals(type)) {
            return null;
        }
        Class<?> normalizedType = normalizeCollectionType(type);
        if (modelsByType.containsKey(normalizedType)) {
            return modelsByType.get(normalizedType);
        }
        if (normalizedType.isEnum()) {
            Model enumModel = new Model();
            enumModel.setName(normalizedType.getSimpleName());
            enumModel.setText(normalizedType.getSimpleName());
            enumModel.setClassName(normalizedType.getName());
            enumModel.setModelType(ModelType.ENUM);
            enumModel.setProperties(List.of());
            enumModel.setEnumValues(enumValuesFor(normalizedType));
            modelsByType.put(normalizedType, enumModel);
            return enumModel;
        }
        Table table = normalizedType.getDeclaredAnnotation(Table.class);
        if (table == null) {
            return null;
        }

        Model model = new Model();
        modelsByType.put(normalizedType, model);
        typesByModel.put(model, normalizedType);
        model.setName(normalizedType.getSimpleName());
        model.setText(normalizedType.getSimpleName());
        model.setClassName(normalizedType.getName());
        model.setTableName(table.value());
        model.setModelType(ModelType.DYNAMIC);
        model.setBaseModel(modelFor(normalizedType.getSuperclass()));
        List<Property> properties = new ArrayList<>();
        for (Field field : modelFields(normalizedType)) {
            Property property = propertyFor(model, field);
            properties.add(property);
            if (isDefaultIdProperty(property) && model.getIdProperty() == null) {
                model.setIdProperty(property);
            }
        }
        model.setProperties(properties);
        model.setAutoSysId(model.getIdProperty() == null);
        model.setShowProperty(legacyShowProperty(model));
        return model;
    }

    private Property legacyShowProperty(Model model) {
        for (Property property : safeProperties(model)) {
            String name = property.getName();
            if (name != null && name.toLowerCase(Locale.ROOT).contains("name")) {
                return property;
            }
        }
        if (model.getIdProperty() != null) {
            return model.getIdProperty();
        }
        return safeProperties(model).isEmpty() ? null : safeProperties(model).get(0);
    }

    private List<EnumValue> enumValuesFor(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null) {
            return List.of();
        }
        List<EnumValue> values = new ArrayList<>();
        for (int index = 0; index < constants.length; index++) {
            EnumValue value = new EnumValue();
            value.setName(((Enum<?>) constants[index]).name());
            value.setValue(String.valueOf(index));
            values.add(value);
        }
        return values;
    }

    private boolean isDefaultIdProperty(Property property) {
        Field field = fieldsByProperty.get(property);
        if (field == null) {
            return false;
        }
        if (field.getDeclaredAnnotation(Id.class) != null) {
            return true;
        }
        for (Column column : field.getDeclaredAnnotationsByType(Column.class)) {
            if (column.identify()) {
                return true;
            }
        }
        return false;
    }

    private void wireRelations() {
        for (Model model : modelsByType.values()) {
            if (ModelType.DYNAMIC.equals(model.getModelType())) {
                model.setRelations(relationsFor(model));
            }
        }
    }

    private List<Relation> relationsFor(Model model) {
        List<Relation> relations = new ArrayList<>();
        for (Property property : safeProperties(model)) {
            if (Boolean.TRUE.equals(property.getIsCollection())
                    && property.getPropertyModel() != null) {
                Relation relation = new Relation();
                relation.setProperty(property);
                Field field = fieldsByProperty.get(property);
                MultiType multiType = field == null ? null : field.getDeclaredAnnotation(MultiType.class);
                ReferToProperty referToProperty = field == null
                        ? null
                        : field.getDeclaredAnnotation(ReferToProperty.class);
                Property targetProperty;
                if (multiType != null) {
                    relation.setRelationType(RelationType.Many2Many);
                    relation.setRelationTable(relationTable(model, property.getPropertyModel()));
                    relation.setPropertyColumn(legacyTableName(property.getPropertyModel()) + "_ID");
                    relation.setTargetColumn(legacyTableName(model) + "_ID");
                } else if (referToProperty != null) {
                    targetProperty = referencedProperty(property.getPropertyModel(), referToProperty.value());
                    relation.setRelationType(RelationType.One2Many);
                    relation.setRelationTable(property.getPropertyModel().getTableName());
                    relation.setPropertyColumn(legacyTableName(property.getPropertyModel()) + "_ID");
                    relation.setTargetColumn(targetProperty.getColumn());
                    relation.setTargetProperty(targetProperty);
                } else if (property.getPropertyModel() == model) {
                    relation.setRelationType(RelationType.Recurve);
                    relation.setRelationTable(modelTableName(model) + "_" + property.getName());
                    relation.setPropertyColumn(modelTableName(model) + "_" + property.getName().toUpperCase() + "_ITEM");
                    relation.setTargetColumn(oneToManyTargetColumn(model, property));
                } else if ((targetProperty = reciprocalCollectionProperty(model, property.getPropertyModel())) != null) {
                    relation.setRelationType(RelationType.Many2Many);
                    relation.setRelationTable(relationTable(model, property.getPropertyModel()));
                    relation.setPropertyColumn(legacyTableName(property.getPropertyModel()) + "_ID");
                    relation.setTargetColumn(legacyTableName(model) + "_ID");
                    relation.setTargetProperty(targetProperty);
                } else {
                    relation.setRelationType(RelationType.One2Many);
                    relation.setRelationTable(property.getPropertyModel().getTableName());
                    targetProperty = objectWithSubItemTargetProperty(model, property.getPropertyModel());
                    if (targetProperty == null) {
                        relation.setTargetColumn(oneToManyTargetColumn(model, property));
                    } else {
                        relation.setTargetProperty(targetProperty);
                        relation.setTargetColumn(targetProperty.getColumn());
                    }
                }
                relations.add(relation);
            }
        }
        return relations;
    }

    private Property objectWithSubItemTargetProperty(Model sourceModel, Model targetModel) {
        if (usesLegacySysIdTargetColumn(sourceModel)
                || !isObjectWithSubItemType(typesByModel.get(sourceModel))) {
            return null;
        }
        return referencedProperty(targetModel, "Parent");
    }

    private boolean isObjectWithSubItemType(Class<?> type) {
        if (type == null || !(type.getGenericSuperclass() instanceof ParameterizedType parameterizedType)) {
            return false;
        }
        return ObjectWithSubItem.class.equals(parameterizedType.getRawType());
    }

    private Property referencedProperty(Model targetModel, String propertyReference) {
        for (Property candidate : safeProperties(targetModel)) {
            if (matchesPropertyReference(propertyReference, candidate.getName())
                    || matchesPropertyReference(propertyReference, candidate.getColumn())) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Cannot find referenced property "
                + propertyReference
                + " on model "
                + targetModel.getName());
    }

    private boolean matchesPropertyReference(String expected, String actual) {
        return expected != null
                && actual != null
                && (actual.equals(expected) || actual.equalsIgnoreCase(expected));
    }

    private Property reciprocalCollectionProperty(Model sourceModel, Model targetModel) {
        Property reciprocal = null;
        int matches = 0;
        for (Property candidate : safeProperties(targetModel)) {
            if (Boolean.TRUE.equals(candidate.getIsCollection())
                    && candidate.getPropertyModel() == sourceModel) {
                reciprocal = candidate;
                matches++;
            }
        }
        return matches == 1 ? reciprocal : null;
    }

    private String relationTable(Model left, Model right) {
        List<String> tables = new ArrayList<>();
        tables.add(legacyTableName(left));
        tables.add(legacyTableName(right));
        Collections.sort(tables);
        return tables.get(0) + "_" + tables.get(1);
    }

    private String legacyTableName(Model model) {
        return modelTableName(model).replace("[", "").replace("]", "").trim();
    }

    private String oneToManyTargetColumn(Model model, Property property) {
        String sourceTable = modelTableName(model);
        String sourceIdColumn = model.getIdProperty() == null ? null : model.getIdProperty().getColumn();
        if (usesLegacySysIdTargetColumn(model)) {
            return sourceTable + "_" + property.getName() + "_SYSID";
        }
        return sourceTable + "_" + property.getName() + sourceIdColumn;
    }

    private boolean usesLegacySysIdTargetColumn(Model model) {
        String sourceIdColumn = model.getIdProperty() == null ? null : model.getIdProperty().getColumn();
        return Boolean.TRUE.equals(model.getAutoSysId())
                || sourceIdColumn == null
                || "SYSID".equalsIgnoreCase(sourceIdColumn.trim());
    }

    private String modelTableName(Model model) {
        return model.getTableName() == null || model.getTableName().isBlank()
                ? model.getName()
                : model.getTableName();
    }

    private Property propertyFor(Model owner, Field field) {
        boolean collection = isCollection(field.getType());
        Class<?> valueType = collection ? collectionItemType(field) : field.getType();
        Model propertyModel = modelFor(valueType);

        Property property = new Property();
        property.setName(field.getName());
        property.setRemark(field.getName());
        property.setOwner(owner);
        Column[] columns = field.getDeclaredAnnotationsByType(Column.class);
        property.setColumn(columnName(owner, field, columns));
        property.setIsCollection(collection);
        property.setPropertyModel(propertyModel);
        property.setPropertyType(propertyType(field.getType(), valueType, propertyModel));
        property.setAllowDbNull(!field.getType().isPrimitive());
        applyColumnMetadata(owner, property, field, columns);

        Id id = field.getDeclaredAnnotation(Id.class);
        if (id != null) {
            SqlGenerate sqlGenerate = field.getDeclaredAnnotation(SqlGenerate.class);
            if (isDefaultIdGroup(id.value())
                    || (sqlGenerate != null && SqlGenerateConfig.AUTO_INCREMENT.equals(sqlGenerate.value()))) {
                property.setPropertyType(PropertyType.IdentifyId);
            }
            property.setCheck(true);
            property.setAllowDbNull(false);
            property.setIxGroup(isDefaultIdGroup(id.value()) ? "" : id.value());
        }
        Key key = field.getDeclaredAnnotation(Key.class);
        if (key != null) {
            property.setCheck(true);
            property.setIxGroup(key.value());
        }
        fieldsByProperty.put(property, field);
        return property;
    }

    private boolean isDefaultIdGroup(String group) {
        return group == null || group.isBlank() || "Id".equals(group);
    }

    private static List<Field> modelFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Set<String> seenNames = Collections.newSetFromMap(new LinkedHashMap<>());
        for (Class<?> current = type; current != null && !Object.class.equals(current); current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                if (seenNames.add(field.getName())) {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private void applyColumnMetadata(Model owner, Property property, Field field, Column[] columns) {
        if (columns == null || columns.length == 0) {
            return;
        }
        if (columns.length > 1) {
            property.setMultiMap(true);
            property.setColumn(null);
            if (!columns[0].format().isBlank()) {
                property.setFormat(columns[0].format());
            }
            List<MultiDbMap> dbMaps = new ArrayList<>();
            for (Column column : columns) {
                dbMaps.add(new MultiDbMap(column.propertyName(), columnName(owner, column)));
            }
            property.setDbMaps(dbMaps);
            return;
        }
        Column column = columns[0];
        if (column.noMap()) {
            property.setColumn(null);
        }
        if (column.key()) {
            property.setCheck(true);
            property.setIxGroup(column.keyGroupName());
        }
        if (column.keyCanBeNullOrEmpty()) {
            property.setKeysCanBeDefault(true);
        }
        if (column.identify()) {
            if (!UUID.class.equals(field.getType())) {
                property.setPropertyType(PropertyType.IdentifyId);
            }
            property.setCheck(true);
        }
        property.setGenerationType(column.generationType().code());
        if (!column.generationExpression().isBlank()) {
            property.setGenerationExpression(column.generationExpression());
        }
        if (!column.defaultValue().isBlank()) {
            property.setDefaultValue(column.defaultValue());
        }
        if (!column.format().isBlank()) {
            property.setFormat(column.format());
        }
        if (EncryptType.MD5.equals(column.encryptType())) {
            property.setPropertyType(PropertyType.MD5);
        } else if (EncryptType.RADOM_DECS.equals(column.encryptType())) {
            property.setPropertyType(PropertyType.RadomDECS);
        }
    }

    private PropertyType propertyType(Class<?> fieldType, Class<?> valueType, Model propertyModel) {
        if (isCollection(fieldType)) {
            return PropertyType.BusinessObject;
        }
        if (propertyModel != null) {
            return ModelType.ENUM.equals(propertyModel.getModelType())
                    ? PropertyType.Enum
                    : PropertyType.BusinessObject;
        }
        if (Integer.class.equals(valueType) || int.class.equals(valueType)
                || Short.class.equals(valueType) || short.class.equals(valueType)) {
            return PropertyType.Int;
        }
        if (Long.class.equals(valueType) || long.class.equals(valueType)) {
            return PropertyType.Long;
        }
        if (Float.class.equals(valueType) || float.class.equals(valueType)) {
            return PropertyType.Float;
        }
        if (Double.class.equals(valueType) || double.class.equals(valueType)) {
            return PropertyType.Double;
        }
        if (BigDecimal.class.equals(valueType)) {
            return PropertyType.Decimal;
        }
        if (Boolean.class.equals(valueType) || boolean.class.equals(valueType)) {
            return PropertyType.Boolean;
        }
        if (Character.class.equals(valueType) || char.class.equals(valueType)) {
            return PropertyType.Char;
        }
        if (Byte.class.equals(valueType) || byte.class.equals(valueType)) {
            return PropertyType.Byte;
        }
        if (LocalDate.class.equals(valueType)) {
            return PropertyType.Date;
        }
        if (LocalTime.class.equals(valueType)) {
            return PropertyType.Time;
        }
        if (LocalDateTime.class.equals(valueType) || java.util.Date.class.equals(valueType)) {
            return PropertyType.DateTime;
        }
        if (UUID.class.equals(valueType)) {
            return PropertyType.Guid;
        }
        return PropertyType.String;
    }

    private static boolean isCollection(Class<?> type) {
        return type.isArray() || Collection.class.isAssignableFrom(type);
    }

    private static Class<?> normalizeCollectionType(Class<?> type) {
        return type.isArray() ? type.getComponentType() : type;
    }

    private static Class<?> collectionItemType(Field field) {
        if (field.getType().isArray()) {
            return field.getType().getComponentType();
        }
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments().length > 0
                && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> itemType) {
            return itemType;
        }
        return Object.class;
    }

    private String columnName(Model owner, Field field, Column[] columns) {
        if (columns != null && columns.length > 1) {
            return null;
        }
        Column column = columns == null || columns.length == 0 ? null : columns[0];
        if (column != null && column.noMap()) {
            return null;
        }
        if (column != null && column.value() != null && !column.value().isBlank()) {
            return columnName(owner, column);
        }
        return toSnakeCase(field.getName());
    }

    private String columnName(Model owner, Column column) {
        String columnName = column.value();
        Table table = tableAnnotation(owner);
        String prefix = table == null ? "" : table.columnPrefix();
        if (column.preIndex() == -1
                || prefix == null
                || prefix.isBlank()
                || column.overrideParent()) {
            return columnName;
        }
        String insertedPrefix = prefix;
        if (column.preLen() != 0) {
            insertedPrefix = insertedPrefix.substring(0, Math.min(column.preLen(), insertedPrefix.length()));
        }
        int index = Math.max(0, Math.min(column.preIndex(), columnName.length()));
        return columnName.substring(0, index) + insertedPrefix + columnName.substring(index);
    }

    private Table tableAnnotation(Model owner) {
        Class<?> ownerType = typesByModel.get(owner);
        return ownerType == null ? null : ownerType.getDeclaredAnnotation(Table.class);
    }

    private static String toSnakeCase(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (Character.isUpperCase(character) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(character));
        }
        return result.toString();
    }

    private static List<Class<?>> scanPackage(String packageName, ClassLoader classLoader) {
        if (packageName == null || packageName.isBlank()) {
            return List.of();
        }
        ClassLoader loader = classLoader == null
                ? ReflectiveAppModuleSource.class.getClassLoader()
                : classLoader;
        String packagePath = packageName.replace('.', '/');
        try {
            List<String> classNames = new ArrayList<>();
            Enumeration<URL> resources = loader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    scanFilePackage(packageName, resource, classNames);
                } else if ("jar".equals(resource.getProtocol())) {
                    scanJarPackage(packagePath, resource, classNames);
                }
            }
            List<Class<?>> modelTypes = new ArrayList<>();
            classNames.stream()
                    .distinct()
                    .sorted()
                    .map(className -> loadClass(loader, className))
                    .filter(ReflectiveAppModuleSource::isReflectiveModelType)
                    .forEach(modelTypes::add);
            return modelTypes;
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot scan model package: " + packageName, e);
        }
    }

    private static void scanFilePackage(String packageName, URL resource, List<String> classNames) {
        String directoryName = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
        File directory = new File(directoryName);
        scanDirectory(packageName, directory, classNames);
    }

    private static void scanDirectory(String packageName, File directory, List<String> classNames) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(packageName + "." + file.getName(), file, classNames);
            } else if (file.getName().endsWith(".class")) {
                addClassName(packageName + "." + file.getName().substring(0, file.getName().length() - 6), classNames);
            }
        }
    }

    private static void scanJarPackage(String packagePath, URL resource, List<String> classNames) throws IOException {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    addClassName(entryName.substring(0, entryName.length() - 6).replace('/', '.'), classNames);
                }
            }
        }
    }

    private static void addClassName(String className, List<String> classNames) {
        if (!className.endsWith(".package-info") && !className.endsWith(".module-info")) {
            classNames.add(className);
        }
    }

    private static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load model class: " + className, e);
        }
    }

    private static boolean isReflectiveModelType(Class<?> type) {
        return type.isEnum() || type.getDeclaredAnnotation(Table.class) != null;
    }

    private static List<Property> safeProperties(Model model) {
        return model.getProperties() == null ? List.of() : model.getProperties();
    }
}
