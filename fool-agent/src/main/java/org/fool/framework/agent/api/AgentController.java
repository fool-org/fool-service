package org.fool.framework.agent.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.fool.framework.agent.service.AgentCapability;
import org.fool.framework.agent.service.AgentCapabilityType;
import org.fool.framework.agent.service.AgentSession;
import org.fool.framework.agent.service.AgentSessionService;
import org.fool.framework.agent.service.AgentTurnResult;
import org.fool.framework.dto.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final AgentSessionService sessionService;

    public AgentController(AgentSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/capabilities")
    @ResponseBody
    public CommonResponse<List<AgentCapability>> capabilities() {
        return new CommonResponse<>(sessionService.capabilities());
    }

    @PostMapping("/sessions")
    @ResponseBody
    public CommonResponse<AgentSession> start(@RequestBody StartSessionRequest request) {
        return new CommonResponse<>(sessionService.start(request.getToken(), request.getTitle()));
    }

    @PostMapping("/sessions/{sessionId}")
    @ResponseBody
    public CommonResponse<AgentSession> get(@PathVariable String sessionId, @RequestBody SessionRequest request) {
        return new CommonResponse<>(sessionService.get(sessionId, request.getToken()));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    @ResponseBody
    public CommonResponse<AgentTurnResult> message(@PathVariable String sessionId, @RequestBody MessageRequest request) {
        return new CommonResponse<>(sessionService.recordUserMessage(
                sessionId, request.getToken(), request.getCapability(), request.getContent(), request.getContext()));
    }

    @PostMapping("/sessions/{sessionId}/advance")
    @ResponseBody
    public CommonResponse<AgentSession> advance(@PathVariable String sessionId, @RequestBody SessionRequest request) {
        return new CommonResponse<>(sessionService.advance(sessionId, request.getToken()));
    }

    public static class SessionRequest {
        @JsonAlias("Token")
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    public static class StartSessionRequest extends SessionRequest {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class MessageRequest extends SessionRequest {
        private AgentCapabilityType capability;
        private String content;
        private Map<String, Object> context;

        public AgentCapabilityType getCapability() {
            return capability;
        }

        public void setCapability(AgentCapabilityType capability) {
            this.capability = capability;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public void setContext(Map<String, Object> context) {
            this.context = context;
        }
    }
}
