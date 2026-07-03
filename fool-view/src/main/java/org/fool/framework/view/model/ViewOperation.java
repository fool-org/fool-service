package org.fool.framework.view.model;

import lombok.Data;
import org.fool.framework.model.model.Operation;

@Data
public class ViewOperation {
    private int location;
    private String name;
    private boolean requireSelect;
    private ViewOperationType type;
    private View resultView;
    private Operation operation;
    private String confirmMsg;
    private String successMsg;
    private String errorMsg;
}
