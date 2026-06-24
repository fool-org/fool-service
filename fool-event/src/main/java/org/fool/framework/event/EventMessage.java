package org.fool.framework.event;

import lombok.Data;
import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.Id;
import org.fool.framework.common.annotation.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("SW_SYS_MSG")
@Data
public class EventMessage {
    @Id
    @Column("MSG_ID")
    private UUID messageId;
    @Column("MSG_EVT")
    private UUID eventId;
    @Column("MSG_VIEW")
    private String viewId;
    @Column("MSG_OBJ")
    private String objectId;
    @Column("MSG_MSG")
    private String messageFormat;
    @Column("MSG_CREATETIME")
    private LocalDateTime generateTime;
    @Column("MSG_READTIME")
    private LocalDateTime readTime;
    @Column("MSG_PUSHTIME")
    private LocalDateTime pushTime;
    @Column("MSG_ENDLINETIME")
    private LocalDateTime readTimeoutTime;
    @Column("MSG_STATE")
    private MsgState state;
    @Column("MSG_READOPERATION")
    private String readOperationId;
    @Column("MSG_USERID")
    private String notifyUserId;
    @Column("MSG_MSGTYPE")
    private MsgNotifyType notifyType;
}
