package org.fool.framework.model.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("SW_SYS_MULTIMAP")
public class MultiDbMap {
    @Column("MAP_NAME")
    private String propertyName;
    @Column("MAP_COLNAME")
    private String columnName;
}
