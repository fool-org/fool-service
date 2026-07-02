package org.fool.framework.view.dto;

import lombok.Data;
import org.fool.framework.common.PropertyType;
import org.fool.framework.view.model.ItemEditType;

@Data
public class ReadItemViewItemInfo {
    private String name;
    private PropertyType prpType;
    private Integer index;
    private String prpId;
    private Long prpModelId;
    private String id;
    private String prpShowName;
    private boolean readOnly;
    private ItemEditType editType;
}
