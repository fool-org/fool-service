package org.fool.framework.dbmanage;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Table("DB_AppDB")
@Data
public class ApplicationDatabase {
    @Id("appDatabase")
    @Column("App_Id")
    private Long appId;
    @Id("appDatabase")
    @Column("DBNo")
    private String dbNo;
}
