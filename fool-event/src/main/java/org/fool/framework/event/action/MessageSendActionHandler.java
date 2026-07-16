package org.fool.framework.event.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.fool.framework.common.authz.ControlledActionPreview;
import org.fool.framework.common.authz.ControlledActionResult;
import org.fool.framework.event.EventCompany;
import org.fool.framework.event.EventDefinition;
import org.fool.framework.event.EventDefinitionRelationLoader;
import org.fool.framework.event.EventDepartment;
import org.fool.framework.event.EventRecipient;
import org.fool.framework.event.EventRole;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class MessageSendActionHandler implements ControlledActionHandler {
    private final JdbcTemplate jdbcTemplate;
    private final EventDefinitionRelationLoader relationLoader;
    private final ObjectMapper objectMapper;

    public MessageSendActionHandler(JdbcTemplate jdbcTemplate,
                                    EventDefinitionRelationLoader relationLoader,
                                    ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.relationLoader = relationLoader;
        this.objectMapper = objectMapper;
    }

    public String action() { return "message.send"; }
    public String resourceType() { return "Event"; }
    public void preflight(ControlledActionContext context) { plan(context); }
    public String currentSnapshotVersion(ControlledActionContext context) { return plan(context).version(); }

    public ControlledActionPreview preview(ControlledActionContext context) {
        Plan plan = plan(context);
        return new ControlledActionPreview(plan.version(), plan.recipients().size(),
                Map.of("eventDefinitionId", context.resourceId(), "objectId", plan.objectId(),
                        "recipientCount", plan.recipients().size(), "messageTemplateResolvedServerSide", true),
                List.of("event definition is enabled", "recipient set is resolved from server-side relations",
                        "message content comes from the stored event definition"),
                "use the outbox reference to cancel generated messages that remain in GENERATE state",
                List.of("messages already read or processed require manual follow-up"),
                List.of("EXTERNAL_SIDE_EFFECT"));
    }

    @Transactional
    public ControlledActionResult execute(ControlledActionContext context) {
        Plan plan = plan(context);
        requireApprovedSnapshot(context, plan.version());
        String outboxId = UUID.randomUUID().toString();
        try {
            jdbcTemplate.update("""
                    INSERT INTO `FOOL_AGENT_OUTBOX`
                      (`OUTBOX_ID`, `ACTION_REQUEST_ID`, `EVENT_DEFINITION_ID`, `OBJECT_ID`,
                       `PAYLOAD_HASH`, `STATUS`, `RESULT_JSON`, `CREATED_AT`, `UPDATED_AT`)
                    VALUES (?, ?, ?, ?, ?, 'PROCESSING', NULL, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
                    """, outboxId, context.actionRequestId(), context.resourceId(), plan.objectId(), plan.version());
        } catch (DuplicateKeyException ex) {
            throw new ControlledActionException(409, "IDEMPOTENCY_REPLAY");
        }
        List<String> messageIds = new ArrayList<>();
        for (String recipient : plan.recipients()) {
            String messageId = UUID.randomUUID().toString();
            jdbcTemplate.update("""
                    INSERT INTO `SW_SYS_MSG`
                      (`MSG_ID`, `MSG_EVT`, `MSG_VIEW`, `MSG_OBJ`, `MSG_MSG`, `MSG_CREATETIME`,
                       `MSG_ENDLINETIME`, `MSG_STATE`, `MSG_USERID`, `MSG_MSGTYPE`)
                    VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP(),
                            DATE_ADD(CURRENT_TIMESTAMP(), INTERVAL ? SECOND), 0, ?, 0)
                    """, messageId, outboxId, plan.viewId(), plan.objectId(), plan.messageFormat(),
                    plan.timeoutSeconds(), recipient);
            messageIds.add(messageId);
        }
        try {
            jdbcTemplate.update("""
                    UPDATE `FOOL_AGENT_OUTBOX` SET `STATUS` = 'SUCCEEDED', `RESULT_JSON` = ?,
                      `UPDATED_AT` = CURRENT_TIMESTAMP(6) WHERE `OUTBOX_ID` = ?
                    """, objectMapper.writeValueAsString(Map.of("messageIds", messageIds)), outboxId);
        } catch (Exception ex) {
            throw new ControlledActionException(503, "OUTBOX_RESULT_WRITE_FAILED");
        }
        return new ControlledActionResult(outboxId,
                Map.of("outboxId", outboxId, "messageIds", messageIds, "recipientCount", messageIds.size()),
                Map.of("cancelableWhileState", "GENERATE"));
    }

    private Plan plan(ControlledActionContext context) {
        UUID definitionId;
        try { definitionId = UUID.fromString(context.resourceId()); }
        catch (RuntimeException ex) { throw denied("RESOURCE_OUT_OF_SCOPE"); }
        String objectId = context.arguments().get("objectId") == null
                ? "" : String.valueOf(context.arguments().get("objectId")).trim();
        if (!objectId.matches("[A-Za-z0-9._:-]{1,255}")) throw denied("OBJECT_ID_REQUIRED");
        EventDefinition definition = jdbcTemplate.query("""
                SELECT `EVTDEF_ID`, `EVTDEF_VIEW`, `EVTDEF_MSGFMT`, `EVTDEF_TIMEOUTSECS`, `EVTDEF_STATE`
                  FROM `SW_EVT_DEF` WHERE `EVTDEF_ID` = ?
                """, rs -> {
            if (!rs.next()) return null;
            EventDefinition value = new EventDefinition();
            value.setDefId(UUID.fromString(rs.getString("EVTDEF_ID")));
            value.setViewId(rs.getString("EVTDEF_VIEW"));
            value.setMessageFormat(rs.getString("EVTDEF_MSGFMT"));
            value.setTimeoutSeconds(rs.getInt("EVTDEF_TIMEOUTSECS"));
            value.setState(rs.getInt("EVTDEF_STATE") == 0
                    ? org.fool.framework.event.EventState.IsRunning : org.fool.framework.event.EventState.Stopped);
            return value;
        }, context.resourceId());
        if (definition == null || definition.getState() != org.fool.framework.event.EventState.IsRunning) {
            throw denied("EVENT_NOT_ENABLED");
        }
        if (definition.getMessageFormat() == null || definition.getMessageFormat().isBlank()
                || definition.getMessageFormat().length() > 2000) throw denied("MESSAGE_TEMPLATE_INVALID");
        relationLoader.loadRelations(List.of(definition));
        Set<String> recipients = new LinkedHashSet<>();
        definition.getNotifyUsers().forEach(user -> add(recipients, user));
        for (EventRole role : definition.getNotifyRoles()) role.getAuthUsers().forEach(user -> add(recipients, user));
        for (EventDepartment department : definition.getNotifyDepartments()) addDepartment(recipients, department);
        for (EventCompany company : definition.getNotifyCompanies()) {
            for (EventDepartment department : company.getDepartments()) addDepartment(recipients, department);
        }
        if (recipients.isEmpty() || recipients.size() > 100) throw denied("MESSAGE_RECIPIENTS_INVALID");
        List<String> orderedRecipients = recipients.stream().sorted(Comparator.naturalOrder()).toList();
        int timeout = definition.getTimeoutSeconds() == null ? 3600
                : Math.max(60, Math.min(definition.getTimeoutSeconds(), 604800));
        String version = hash(context.resourceId() + "\u0000" + objectId + "\u0000" + definition.getViewId()
                + "\u0000" + definition.getMessageFormat() + "\u0000" + orderedRecipients + "\u0000" + timeout);
        return new Plan(objectId, definition.getViewId(), definition.getMessageFormat(), timeout,
                orderedRecipients, version);
    }

    private static void addDepartment(Set<String> recipients, EventDepartment department) {
        department.getUsers().forEach(user -> add(recipients, user));
        department.getSubDepartments().forEach(child -> addDepartment(recipients, child));
    }
    private static void add(Set<String> recipients, EventRecipient user) {
        if (user != null && user.getUserId() != null && !user.getUserId().isBlank()) recipients.add(user.getUserId());
    }
    private static void requireApprovedSnapshot(ControlledActionContext context, String current) {
        Object expected = context.arguments().get("_approvedSnapshotVersion");
        if (!(expected instanceof String value) || !value.equals(current)) throw new ControlledActionException(409, "OBJECT_CHANGED");
    }
    private static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
    private static ControlledActionException denied(String reason) { return new ControlledActionException(400, reason); }
    private record Plan(String objectId, String viewId, String messageFormat, int timeoutSeconds,
                        List<String> recipients, String version) {}
}
