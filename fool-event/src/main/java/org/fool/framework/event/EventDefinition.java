package org.fool.framework.event;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table("SW_EVT_DEF")
@Data
public class EventDefinition {
    @Id
    @Column("EVTDEF_ID")
    private UUID defId;
    @Column("EVTDEF_FILTER")
    private String filter;
    @Column("EVTDEF_VIEW")
    private String viewId;
    @Column("EVTDEF_OPERATION")
    private String operationId;
    @Column("EVTDEF_MSGFMT")
    private String messageFormat;
    @Column("EVTDEF_TIMEOUTSECS")
    private Integer timeoutSeconds;
    @Column("EVTDEF_MODEL")
    private String modelId;
    @Column("EVTDEF_MODELREF")
    private EventModelRefType modelRefType;
    @Column("EVTDEF_STATE")
    private EventState state;
    private List<EventDepartment> notifyDepartments = new ArrayList<>();
    private List<EventRole> notifyRoles = new ArrayList<>();
    private List<EventRecipient> notifyUsers = new ArrayList<>();
    private List<EventCompany> notifyCompanies = new ArrayList<>();
}
