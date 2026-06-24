package org.fool.framework.dbmanage;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Table("DS_DataSourceSet")
@Data
public class DataBaseSource {
    @Id
    @Column("DS_Key")
    private String key;
    @Column("DS_DBNo")
    private String dbNo;
}
