package org.fool.framework.model.model;

import lombok.Data;
import lombok.ToString;
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
    @ToString.Exclude
    @Column("default_owner")
    private Model owner;
    @Column("owner")
    private List<Property> properties;
    private Property idProperty;
    private transient Property showProperty;
    private transient List<Relation> relations;
    private transient List<Operation> operations;
    private transient List<Trigger> triggers;
    @Column("owner")
    private List<EnumValue> enumValues = new ArrayList<>();
}
