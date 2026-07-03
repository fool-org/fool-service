package org.fool.framework.application.api;

import lombok.Data;
import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.event.EventMessage;
import org.fool.framework.event.EventMessageRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {
    private final AuthService authService;
    private final EventMessageRepository messageRepository;

    public MessageController(AuthService authService, EventMessageRepository messageRepository) {
        this.authService = authService;
        this.messageRepository = messageRepository;
    }

    @PostMapping("/getmsg")
    public CommonResponse<GetMessageResult> getMessages(@RequestBody CommonRequest request) {
        String userId = authService.getInfoByToken(request.getToken()).getId();
        List<EventMessage> messages = messageRepository.findGeneratedForUser(userId, 1);
        LocalDateTime pushedAt = LocalDateTime.now();
        messages.forEach(message -> messageRepository.markPushed(message.getMessageId(), pushedAt));
        return new CommonResponse<>(new GetMessageResult(messages.stream().map(this::toInfo).toList()));
    }

    @PostMapping("/getnotify")
    public CommonResponse<GetNotifyResult> getNotify(@RequestBody CommonRequest request) {
        // ponytail: legacy DataService.GetNotify throws NotImplementedException; keep the migrated shell empty.
        authService.getInfoByToken(request.getToken());
        return new CommonResponse<>(new GetNotifyResult(List.of()));
    }

    private MessageInfo toInfo(EventMessage message) {
        MessageInfo info = new MessageInfo();
        info.setMessageID(String.valueOf(message.getMessageId()));
        info.setGernerationTime(message.getGenerateTime());
        info.setMessageContent(message.getMessageFormat());
        info.setResultView(longOrZero(message.getViewId()));
        info.setResultKey(message.getObjectId());
        info.setRead(false);
        info.setTimeOut(false);
        return info;
    }

    private static long longOrZero(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Long.parseLong(value);
    }

    @Data
    public static class GetMessageResult {
        private final List<MessageInfo> messages;
    }

    @Data
    public static class GetNotifyResult {
        private final List<NotifyInfo> notifies;
    }

    @Data
    public static class NotifyInfo {
        private int count;
        private String authNo;
    }

    @Data
    public static class MessageInfo {
        private String messageID;
        private LocalDateTime gernerationTime;
        private String messageContent;
        private long resultView;
        private String objId;
        private String resultViewType;
        private String resultKey;
        private boolean read;
        private boolean timeOut;
        private LocalDateTime readDateTime = LocalDateTime.now();
    }
}
