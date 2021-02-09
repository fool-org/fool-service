package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.*;

import java.util.List;


@Data
@Table("fool_sys_model")
public class Model {
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    private Long id;
    @Id
    private String name;
    private String text;
    private String remark;
    private ModelType modelType;
    private String className;
    private String tableName;
    @Column("owner")
    private List<Property> properties;
    private Property idProperty;
//    private List<EnumValue> enums;
//    private List<Operation> operations;
//    private List<Trigger> triggers;
}
