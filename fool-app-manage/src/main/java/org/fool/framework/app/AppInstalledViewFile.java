package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_VIEW_FILE")
@Data
public class AppInstalledViewFile {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("VIEW_FILE_ID")
    private Long fileId;
    @Column("VIEW_FILE_NAME")
    private String name;
    @Column("VIEW_FILE_VIEWTYPE")
    private Integer viewType;
    @Column("VIEW_FILE_FILENAME")
    private String fileName;
    @Column("VIEW_FILE_FILECONTENT")
    private String fileContent;
}
