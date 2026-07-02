package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_VIEW")
@Data
public class AppInstalledView {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("VIEW_ID")
    private Long viewId;
    @Column("VIEW_MODEL")
    private Long modelId;
    @Column("VIEW_NAME")
    private String name;
    @Column("VIEW_FILTER")
    private String filter;
    @Column("VIEW_DEFAULT")
    private Long defaultViewId;
    @Column("VIEW_TYPE")
    private Integer viewType;
    @Column("VIEW_CONTYPE")
    private Integer connectionType;
    @Column("VIEW_FILE")
    private Long fileId;
    @Column("VIEW_CHECKAUTH")
    private Boolean checkAuth;
    @Column("VIEW_AUTOFRESHINTERVAL")
    private Integer autoFreshInterval;
    @Column("VIEW_CANEDIT")
    private Boolean canEdit;
}
