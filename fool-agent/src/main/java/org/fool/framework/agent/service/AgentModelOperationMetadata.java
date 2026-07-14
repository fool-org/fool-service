package org.fool.framework.agent.service;

public class AgentModelOperationMetadata {
    private final Long operationId;
    private final String name;
    private final String filter;
    private final Integer baseType;
    private final Long argModelId;
    private final String argFilter;
    private final String invokeDll;
    private final String invokeClass;
    private final String invokeMethod;
    private final Long returnModelId;
    private final Integer commandCount;

    public AgentModelOperationMetadata(Long operationId,
                                       String name,
                                       String filter,
                                       Integer baseType,
                                       Long argModelId,
                                       String argFilter,
                                       String invokeDll,
                                       String invokeClass,
                                       String invokeMethod,
                                       Long returnModelId,
                                       Integer commandCount) {
        this.operationId = operationId;
        this.name = name;
        this.filter = filter;
        this.baseType = baseType;
        this.argModelId = argModelId;
        this.argFilter = argFilter;
        this.invokeDll = invokeDll;
        this.invokeClass = invokeClass;
        this.invokeMethod = invokeMethod;
        this.returnModelId = returnModelId;
        this.commandCount = commandCount;
    }

    public Long getOperationId() {
        return operationId;
    }

    public String getName() {
        return name;
    }

    public String getFilter() {
        return filter;
    }

    public Integer getBaseType() {
        return baseType;
    }

    public Long getArgModelId() {
        return argModelId;
    }

    public String getArgFilter() {
        return argFilter;
    }

    public String getInvokeDll() {
        return invokeDll;
    }

    public String getInvokeClass() {
        return invokeClass;
    }

    public String getInvokeMethod() {
        return invokeMethod;
    }

    public Long getReturnModelId() {
        return returnModelId;
    }

    public Integer getCommandCount() {
        return commandCount;
    }
}
