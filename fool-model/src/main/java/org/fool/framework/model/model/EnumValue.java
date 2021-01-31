package org.fool.framework.model.model;


import lombok.Data;
import org.fool.framework.common.annotation.Table;

@Data
@Table("fool_sys_model_enum")
public class EnumValue {
    private String name;
    private String value;
    private String remark;
}
