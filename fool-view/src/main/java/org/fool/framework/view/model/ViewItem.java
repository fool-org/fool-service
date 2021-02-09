package org.fool.framework.view.model;


import lombok.Data;
import org.fool.framework.common.annotation.SqlGenerate;
import org.fool.framework.common.annotation.SqlGenerateConfig;
import org.fool.framework.common.annotation.Table;

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


    private Long viewId;
}
