package org.fool.framework.dbmanage;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Table("DB_App")
@Data
public class DbApplication {
    @Id
    @Column("BO_Id")
    private Long id;
    @Column("BO_AppName")
    private String appName;
}
