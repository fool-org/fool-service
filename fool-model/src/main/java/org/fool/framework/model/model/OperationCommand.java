package org.fool.framework.model.model;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

@Data
@Table("SW_SYS_COMMANDS")
public class OperationCommand {
    @Id
    @Column("SysId")
    private Long id;
    @Column("SW_SYS_OPERATION_CommandsSysId")
    private Long ownerOperationId;
    @Column("SW_SYS_COMMAND_TYPE")
    private CommandsType commandType;
    @Column("SW_SYS_COMMAND_PROPERTY")
    private Long propertyId;
    @Column("SW_SYS_COMMAND_EXP")
    private String expression;
    @Column("SW_SYS_COMMAND_INDEX")
    private Integer index;
}
