package org.fool.framework.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class SqlTransAutoMic {
    private Object command;
    private Object object;
    private SqlOperation operation;
}
