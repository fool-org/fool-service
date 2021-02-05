package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Table;

import java.util.List;


@Data
@Table("fool_sys_model")
public class Model {
    private Long id;
    private String name;
    private String text;
    private String remark;
    private ModelType modelType;
    private String className;
    private String tableName;
    private List<Property> properties;
    private List<EnumValue> enums;
    private List<Operation> operations;
    private List<Trigger> triggers;
}
