package org.fool.framework.model.model;


import lombok.Data;
import lombok.ToString;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

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
    private String format;
    private String column;
    private List<Trigger> triggerList;
}
