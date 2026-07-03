package org.fool.framework.view.dto;

import lombok.Data;

import java.util.List;

@Data
public class LegacyRunOperationResult {
    private List<ListDataValue> value;
    private boolean success;
    private String returnObjId;
    private long returnViewId;
    private String returnMsg = "";
}
