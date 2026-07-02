package org.fool.framework.app;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

@Table("SW_SYS_VIEW_ITEM")
@Data
public class AppInstalledViewItem {
    @Id
    @SqlGenerate(SqlGenerateConfig.AUTO_INCREMENT)
    @Column("SysId")
    private Long itemId;
    @Column("SW_SYS_VIEW_ItemsVIEW_ID")
    private Long ownerViewId;
    @Column("VIEW_ITEM_NAME")
    private String name;
    @Column("VIEW_ITEM_NOTE")
    private String note;
    @Column("VIEW_ITEM_FORMAT")
    private String format;
    @Column("VIEW_ITEM_PROPERTY")
    private Long propertyId;
    @Column("VIEW_ITEM_PROPERTY_SHOW")
    private Long showPropertyId;
    @Column("VIEW_ITEM_PROPERTY_VALUE")
    private Long valuePropertyId;
    @Column("VIEW_ITEM_READONLY")
    private Boolean readOnly;
    @Column("VIEW_ITEM_INDEX")
    private Integer showIndex;
    @Column("VIEW_ITEM_SUBVIEW")
    private Long listViewId;
    @Column("VIEW_ITEM_EDITVIEW")
    private Long editViewId;
    @Column("VIEW_ITEM_SELECTVIEW")
    private Long selectedViewId;
    @Column("VIEW_ITEM_WIDTH")
    private Integer width;
    @Column("VIEW_ITEM_ISSHOW")
    private Boolean show;
    @Column("VIEW_ITEM_FILE")
    private Long fileId;
    @Column("VIEW_ITEM_EDITTYPE")
    private Integer editType;
    @Column("VIEW_ITEM_SOURCEEXP")
    private String sourceExpression;
}
