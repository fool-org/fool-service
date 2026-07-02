package org.fool.framework.model.model;

import lombok.Data;

@Data
public class Operation {
    private Long id;
    private String name;
    private OperationBaseType baseOperationType = OperationBaseType.NULL;
}
