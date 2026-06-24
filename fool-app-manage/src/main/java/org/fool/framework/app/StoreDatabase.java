package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.util.List;

@Table("SW_STOREDB")
@Data
public class StoreDatabase {
    @Id
    @Column("SW_STORE_STOREID")
    private String storeBaseId;
    @Column("SW_STORE_NAME")
    private String name;
    @Column("SW_STORE_CON")
    private String connection;
    @Column("SW_STORE_Note")
    private String note;
    private List<ApplicationDefinition> apps;
}
