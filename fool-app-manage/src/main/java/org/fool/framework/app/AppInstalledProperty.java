package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.Property;

@Table("SW_SYS_PROPERTY")
@Data
public class AppInstalledProperty {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long propertyId;
    @Column("PROPERTY_TYPE")
    private PropertyType propertyType;
    @Column("PROPERTY_CONTYPE")
    private Integer connectionType;
    @Column("PROPERTY_NAME")
    private String name;
    @Column("PROPERTY_MODEL")
    private Long propertyModelId;
    @Column("PROPERTY_ISARRAY")
    private Boolean array;
    @Column("PROPERTY_COLNAME")
    private String columnName;
    @Column("PROPERTY_PROPERTYNAME")
    private String propertyName;
    @Column("PROPERTY_MULTIMAP")
    private Boolean multiMap;
    @Column("PROPERTY_IXGRPOUP")
    private String ixGroup;
    @Column("PROPERTY_ISCHECK")
    private Boolean check;
    @Column("PROPERTY_GENERATIONTYPE")
    private Integer generationType;
    @Column("PROPERTY_ALLOWDBNULL")
    private Boolean allowDbNull;
    @Column("PROPERTY_CANGET")
    private Boolean canGet;
    @Column("PROPERTY_CANSET")
    private Boolean canSet;
    @Column("PROPERTY_FILTER")
    private String filter;
    @Column("PROPERTY_SOURCE")
    private String source;
    @Column("PROPERTY_FORMAT")
    private String format;
    @Column("PROPERTY_SQLCON")
    private String propertySqlCon;
    @Column("SW_SYS_MODEL_PropertiesSysId")
    private Long ownerModelId;

    public static AppInstalledProperty fromProperty(
            Property source,
            AppInstalledModel owner,
            Long propertyModelId,
            Integer connectionType,
            String connection) {
        AppInstalledProperty property = new AppInstalledProperty();
        property.setPropertyType(source.getPropertyType());
        property.setConnectionType(connectionType);
        property.setName(source.getName());
        property.setPropertyModelId(propertyModelId);
        property.setArray(Boolean.TRUE.equals(source.getIsCollection()));
        property.setColumnName(source.getColumn());
        property.setPropertyName(source.getName());
        property.setMultiMap(Boolean.TRUE.equals(source.getMultiMap()));
        property.setIxGroup(source.getIxGroup());
        property.setCheck(Boolean.TRUE.equals(source.getCheck()));
        property.setGenerationType(source.getGenerationType());
        property.setAllowDbNull(source.getAllowDbNull() == null || Boolean.TRUE.equals(source.getAllowDbNull()));
        property.setCanGet(true);
        property.setCanSet(true);
        property.setFilter(source.getFilter());
        property.setSource(source.getSource());
        property.setFormat(source.getFormat());
        property.setPropertySqlCon(connection);
        property.setOwnerModelId(owner.getModelId());
        return property;
    }
}
