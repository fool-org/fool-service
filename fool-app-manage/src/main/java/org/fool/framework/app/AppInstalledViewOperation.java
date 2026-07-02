package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_VIEW_OPERATION")
@Data
public class AppInstalledViewOperation {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long viewOperationId;
    @Column("SW_SYS_VIEW_OperationsVIEW_ID")
    private Long ownerViewId;
    @Column("SW_VIEW_OPERATION_NAME")
    private String name;
    @Column("SW_VIEW_OPERATION_MODELOPERATION")
    private Long operationViewId;
    @Column("SW_VIEW_OPERATION_RESULTVIEW")
    private Long resultViewId;
    @Column("SW_VIEW_OPERATION_SHOWPROCESS")
    private Boolean showProcess;
    @Column("SW_VIEW_OPERATION_INDEX")
    private Integer location;
    @Column("SW_VIEW_OPERATION_REQUIRESELECTB")
    private Boolean requireSelect;
    @Column("SW_VIEW_OPERATION_IMAGE")
    private String image;
}
