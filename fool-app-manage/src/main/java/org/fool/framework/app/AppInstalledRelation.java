package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.Relation;
import org.fool.framework.model.model.RelationType;

@Table("SW_SYS_RELATION")
@Data
public class AppInstalledRelation {
    @Column("SW_SYS_RELATION_TYPE")
    private RelationType relationType;
    @Column("SW_SYS_RELATION_SOURCEPROPERTY")
    private Long sourcePropertyId;
    @Column("SW_SYS_RELATION_TARGETPROPERTY")
    private Long targetPropertyId;
    @Column("SW_SYS_RELATION_TABLE")
    private String relationTable;
    @Column("SW_SYS_RELATION_SOURCECOL")
    private String propertyColumn;
    @Column("SW_SYS_RELATION_TARGETCOL")
    private String targetColumn;
    @Column("SW_SYS_RELATION_CANBENULL")
    private Boolean canBeNull;

    public static AppInstalledRelation fromRelation(
            Relation source,
            Long sourcePropertyId,
            Long targetPropertyId) {
        AppInstalledRelation relation = new AppInstalledRelation();
        relation.setRelationType(source.getRelationType());
        relation.setSourcePropertyId(sourcePropertyId);
        relation.setTargetPropertyId(targetPropertyId);
        relation.setRelationTable(source.getRelationTable());
        relation.setPropertyColumn(source.getPropertyColumn());
        relation.setTargetColumn(source.getTargetColumn());
        relation.setCanBeNull(Boolean.TRUE.equals(source.getCanBeNull()));
        return relation;
    }
}
