package org.fool.framework.model.model;

import lombok.Data;

@Data
public class Operation {
    private String name;
    private OperationBaseType baseOperationType = OperationBaseType.NO_OPERATION;
}
