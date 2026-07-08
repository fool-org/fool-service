package org.fool.framework.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LegacyRunOperationResult {
    private List<ListDataValue> value;
    private boolean success;
    private String returnObjId;
    private long returnViewId;
    private String returnMsg = "";

    @JsonProperty("Value")
    public List<ListDataValue> getLegacyValue() {
        return value;
    }

    @JsonProperty("IsSuccess")
    public boolean getLegacySuccess() {
        return success;
    }

    @JsonProperty("ReturnObjId")
    public String getLegacyReturnObjId() {
        return returnObjId;
    }

    @JsonProperty("ReturnViewId")
    public long getLegacyReturnViewId() {
        return returnViewId;
    }

    @JsonProperty("ReturnMsg")
    public String getLegacyReturnMsg() {
        return returnMsg;
    }
}
