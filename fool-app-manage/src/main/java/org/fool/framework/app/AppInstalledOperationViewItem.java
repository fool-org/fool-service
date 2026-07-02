package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_OPERATIONVIEW_ITEM")
@Data
public class AppInstalledOperationViewItem {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long itemId;
    @Column("SW_SYS_OPERATIONVIEW_ParamsSysId")
    private Long ownerOperationViewId;
    @Column("SW_SYS_OPVIEWITEM_NAME")
    private String paramName;
    @Column("SW_SYS_OPVIEWITEM_INDEX")
    private Integer index;
    @Column("SW_SYS_OPVIEWITEM_PARAM")
    private Long paramId;
}
