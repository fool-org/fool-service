package org.fool.framework.agent.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.agent.action.ActionRequestService;
import org.fool.framework.agent.action.ActionRequestView;
import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.EffectiveSubject;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.dto.CommonResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/actions")
public class ActionRequestController {
    private final ActionRequestService service;
    private final ObjectMapper objectMapper;

    public ActionRequestController(ActionRequestService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public CommonResponse<ActionRequestView> create(
            @RequestBody JsonNode intent,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Action-Source", defaultValue = "UI") String source,
            @RequestHeader(value = "X-Agent-Session-Id", required = false) String agentSessionId) {
        try {
            return new CommonResponse<>(service.create(subject(), objectMapper.writeValueAsString(intent),
                    source, agentSessionId, idempotencyKey));
        } catch (ControlledActionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ControlledActionException(400, "ACTION_INTENT_JSON_INVALID");
        }
    }

    @PostMapping("/{id}/preview")
    public CommonResponse<ActionRequestView> preview(@PathVariable String id) {
        return new CommonResponse<>(service.preview(subject(), id));
    }

    @GetMapping("/{id}")
    public CommonResponse<ActionRequestView> get(@PathVariable String id) {
        return new CommonResponse<>(service.get(subject(), id));
    }

    @PostMapping("/{id}/confirm")
    public CommonResponse<ActionRequestView> confirm(@PathVariable String id) {
        return new CommonResponse<>(service.confirm(subject(), id));
    }

    @PostMapping("/{id}/execute")
    public CommonResponse<ActionRequestView> execute(@PathVariable String id) {
        return new CommonResponse<>(service.execute(subject(), id));
    }

    @PostMapping("/{id}/approvals")
    public CommonResponse<ActionRequestView> approve(@PathVariable String id,
                                                     @RequestBody ApprovalRequest request) {
        return new CommonResponse<>(service.approve(
                subject(), id, request == null ? null : request.decision,
                request == null ? null : request.comment));
    }

    @PostMapping("/{id}/cancel")
    public CommonResponse<ActionRequestView> cancel(@PathVariable String id) {
        return new CommonResponse<>(service.cancel(subject(), id));
    }

    private static EffectiveSubject subject() {
        return EffectiveSubjectContext.require();
    }

    public static class ApprovalRequest {
        public String decision;
        public String comment;
    }
}
