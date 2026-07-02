package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.MultiDbMap;

@Table("SW_SYS_MULTIMAP")
@Data
public class AppInstalledMultiDbMap {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long mapId;
    @Column("MAP_NAME")
    private String propertyName;
    @Column("MAP_COLNAME")
    private String columnName;
    @Column("SW_SYS_PROPERTY_DBMapsSysId")
    private Long ownerPropertyId;

    public static AppInstalledMultiDbMap fromDbMap(MultiDbMap source, Long ownerPropertyId) {
        AppInstalledMultiDbMap map = new AppInstalledMultiDbMap();
        map.setPropertyName(source.getPropertyName());
        map.setColumnName(source.getColumnName());
        map.setOwnerPropertyId(ownerPropertyId);
        return map;
    }
}
