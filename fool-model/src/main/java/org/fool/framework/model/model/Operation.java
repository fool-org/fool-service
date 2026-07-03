package org.fool.framework.model.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Operation {
    private Long id;
    private String name;
    private String filter;
    private OperationBaseType baseOperationType = OperationBaseType.NULL;
    private Long argModelId;
    private String argFilter;
    private String invokeDll;
    private String invokeClass;
    private String invokeMethod;
    private Long returnModelId;
    private List<OperationCommand> commands = new ArrayList<>();
}
