package org.fool.framework.model.model;


import lombok.Data;
import lombok.ToString;
import org.fool.framework.common.PropertyType;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("fool_sys_model_property")
public class Property {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    private Long id;
    private String name;
    private String remark;
    private Model propertyModel;
    private Boolean isCollection;
    @ToString.Exclude
    private Model owner;
    private String filter;
    private String source;
    private String format;
    private String column;
    private PropertyType propertyType;
    private Boolean allowDbNull = true;
    @Column("is_check")
    private Boolean check = false;
    @Column(noMap = true)
    private transient Boolean keysCanBeDefault = false;
    private String ixGroup;
    private Integer generationType;
    private String generationExpression;
    private String defaultValue;
    private Boolean multiMap = false;
    @Column("SW_SYS_PROPERTY_DBMapsSysId")
    private List<MultiDbMap> dbMaps = new ArrayList<>();
    @Column(noMap = true)
    private List<PropertyTrigger> triggerList = new ArrayList<>();


}
