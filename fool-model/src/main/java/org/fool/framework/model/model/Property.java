package org.fool.framework.model.model;


import lombok.Data;
import org.fool.framework.common.annotation.Table;

import java.util.List;

@Data
@Table("fool_sys_model_property")
public class Property {
    private String name;
    private String remark;
    private Model propertyModel;
    private Boolean isCollection;
    private Model owner;
    private String filter;
    private String format;
    private String column;
    private List<Trigger> triggerList;
}
