package org.fool.framework.model.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Operation {
    private Long id;
    private String name;
    private OperationBaseType baseOperationType = OperationBaseType.NULL;
    private List<OperationCommand> commands = new ArrayList<>();
}
