package org.fool.framework.view.model;


import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;
import org.fool.framework.model.model.Property;

@Data
@Table("fool_sys_view_item")
public class ViewItem {

    /**
     * ID
     */
    @SqlGenerate(SqlGenerateConfig.INSERT)
    private Long id;
    /**
     * 名称
     */
    private String itemName;
    private String itemLabel;
    private String itemLegend;
    private String modelProperty;
    private InputType inputType;
    private boolean canEdit;
    private String selectViewName;
    private String inputRegx;
    private String formatRegx;
    private ItemEditType editType = ItemEditType.ReadOnly;
    @Column("show_index")
    private Integer showIndex = 0;
    @Column("width")
    private Integer width = 0;
    @Column("source_expression")
    private String sourceExpression;
    @Column("list_view_id")
    private Long listViewId;
    @Column("edit_view_id")
    private Long editViewId;
    @Column("selected_view_id")
    private Long selectedViewId;
    @Column(noMap = true)
    private transient Integer listViewType;
    @Column(noMap = true)
    private transient String listViewName;
    @Column(noMap = true)
    private transient Property property;
    @Column(noMap = true)
    private transient String viewFile;


    private Long viewId;
}
