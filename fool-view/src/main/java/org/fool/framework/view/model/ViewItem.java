package org.fool.framework.view.model;


import lombok.Data;
import org.fool.framework.common.annotation.Table;

@Data
@Table("sys_mvc_view_item")
public class ViewItem {
    private String text;
    private String label;
    private InputType inputType;
    private boolean canEdit;
    private String selectViewName;
    private List<OptionItem> optionItemList;
    private String inputRegx;

}
