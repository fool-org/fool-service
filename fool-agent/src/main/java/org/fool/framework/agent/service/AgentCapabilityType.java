package org.fool.framework.agent.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentCapabilityType {
    REPORT_QUERY(10, "report-query", "报表/查询", "fool-report,fool-query",
            "只读报表、查询条件、结果列和报表口径配置草案。"),
    FORM_VIEW(20, "form-view", "表单/视图", "fool-view",
            "列表、详情、新建、子项、操作按钮和 View 元数据配置草案。"),
    MODEL(30, "model", "模型", "fool-model",
            "模型字段、关系、默认值、校验和 DDL 影响评估草案。"),
    DATA_SOURCE(40, "data-source", "数据源", "fool-db-manage",
            "工作数据库、连接目录、路由和凭据接入计划。"),
    EVENT_AUTOMATION(50, "event-automation", "事件/自动化", "fool-event",
            "事件定义、通知对象、触发条件和自动化执行计划。");

    private final int order;
    private final String id;
    private final String displayName;
    private final String ownerModules;
    private final String intent;

    AgentCapabilityType(int order, String id, String displayName, String ownerModules, String intent) {
        this.order = order;
        this.id = id;
        this.displayName = displayName;
        this.ownerModules = ownerModules;
        this.intent = intent;
    }

    public int getOrder() {
        return order;
    }

    public String getId() {
        return id;
    }

    @JsonValue
    public String jsonValue() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getOwnerModules() {
        return ownerModules;
    }

    public String getIntent() {
        return intent;
    }

    @JsonCreator
    public static AgentCapabilityType fromJson(String value) {
        if (value == null) {
            return null;
        }
        for (AgentCapabilityType type : values()) {
            if (type.id.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown agent capability: " + value);
    }
}
