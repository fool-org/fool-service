package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Data
@Table("SW_SYS_RELATION")
public class Relation {
    @Column("SW_SYS_RELATION_TYPE")
    private RelationType relationType;
    @Column("SW_SYS_RELATION_SOURCEPROPERTY")
    private Property property;
    @Column("SW_SYS_RELATION_TARGETPROPERTY")
    private Property targetProperty;
    @Column("SW_SYS_RELATION_TABLE")
    private String relationTable;
    @Column("SW_SYS_RELATION_SOURCECOL")
    private String propertyColumn;
    @Column("SW_SYS_RELATION_TARGETCOL")
    private String targetColumn;
    @Column("SW_SYS_RELATION_CANBENULL")
    private Boolean canBeNull = false;
}
