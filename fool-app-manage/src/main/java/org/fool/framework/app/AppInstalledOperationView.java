package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_OPERATIONVIEW")
@Data
public class AppInstalledOperationView {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long operationViewId;
    @Column("SW_SYS_OPVIEW_NAME")
    private String name;
    @Column("SW_SYS_OPVIEW_RESULT")
    private Long resultViewId;
    @Column("SW_SYS_OPVIEW_OPREATION")
    private Long operationId;
    @Column("SW_SYS_OPVIEW_SUCCESMSG")
    private String successMsg;
    @Column("SW_SYS_OPVIEW_ERRORMSG")
    private String errorMsg;
    @Column("SW_SYS_OPVIEW_MSG")
    private String msg;
    @Column("SW_SYS_OPVIEW_SHOW")
    private Boolean show;
    @Column("SW_SYS_OPVIEW_ConfirmMSG")
    private String confirmMsg;
}
