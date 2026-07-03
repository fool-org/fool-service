package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.ConnectionType;
import org.fool.framework.model.model.Model;

@Table("SW_SYS_MODEL")
@Data
public class AppInstalledModel {
    public static final int CONNECTION_TYPE_APP_SYS = ConnectionType.APP_SYS.code();
    public static final int CONNECTION_TYPE_CURRENT = ConnectionType.CURRENT.code();

    @Id
    @SqlGenerate
    @Column("MODEL_ID")
    private Long modelId;
    @Column("MODEL_NAME")
    private String modelName;
    @Column("MODEL_CLASS")
    private String className;
    @Column("MODEL_CONTYPE")
    private Integer connectionType;
    @Column("MODEL_DATABASETABLE")
    private String tableName;
    @Column("MODEL_MODULE")
    private String moduleName;
    @Column("MODEL_AUTOID")
    private Boolean autoSysId;
    @Column("MODEL_CON")
    private String connection;
    @Column("MODEL_DEFAULTOWNER")
    private Long defaultOwnerId;

    public static AppInstalledModel legacyRootModel(
            String modelName,
            String className,
            String tableName,
            String moduleName,
            Integer connectionType,
            String connection) {
        AppInstalledModel model = new AppInstalledModel();
        model.setModelName(modelName);
        model.setClassName(className);
        model.setTableName(tableName);
        model.setModuleName(moduleName);
        model.setConnectionType(connectionType);
        model.setAutoSysId(false);
        model.setConnection(connection);
        return model;
    }

    public static AppInstalledModel fromModel(
            Model source,
            String moduleName,
            Integer connectionType,
            String connection) {
        AppInstalledModel model = new AppInstalledModel();
        model.setModelName(source.getName());
        model.setClassName(source.getClassName());
        model.setTableName(source.getTableName());
        model.setModuleName(moduleName);
        model.setConnectionType(connectionType);
        model.setAutoSysId(Boolean.TRUE.equals(source.getAutoSysId()));
        model.setConnection(connection);
        model.setDefaultOwnerId(source.getOwner() == null ? null : source.getOwner().getId());
        return model;
    }
}
