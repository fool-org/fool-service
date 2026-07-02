package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.*;

import java.util.ArrayList;
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
    private Boolean autoSysId = false;
    private transient Model baseModel;
    @Column("owner")
    private List<Property> properties;
    private Property idProperty;
    private transient Property showProperty;
    private transient List<Relation> relations;
    private transient List<Operation> operations;
    @Column("owner")
    private List<EnumValue> enumValues = new ArrayList<>();
//    private List<Trigger> triggers;
}
