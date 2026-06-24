package org.fool.framework.event;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table("SW_EVT_EVENT")
@Data
public class EventRecord {
    @Id
    @Column("EVT_ID")
    private UUID eventId;
    @Column("EVT_CREATETIME")
    private LocalDateTime generationTime;
    @Column("EVT_MSG")
    private String eventMessage;
    @Column("EVT_DEALMSG")
    private String dealOperationText;
    @Column("EVT_DEALTIME")
    private LocalDateTime lastDealTime;
    @Column("EVT_DEALUSER")
    private String lastDealUser;
    @Column("EVT_VIEW")
    private String viewId;
    @Column("EVT_DEF")
    private String objectId;
    @Column("EVT_Defination")
    private UUID definitionId;
    private EventDefinition definition;
    private List<EventMessage> messages;
}
