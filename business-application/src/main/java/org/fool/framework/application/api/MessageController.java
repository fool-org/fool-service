package org.fool.framework.application.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.fool.framework.common.authz.EffectiveSubjectContext;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.event.EventMessage;
import org.fool.framework.event.EventMessageRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
public class MessageController {
    private final EventMessageRepository messageRepository;

    public MessageController(EventMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping({"/api/v1/message/getmsg", "/api/v1/getmsg"})
    public CommonResponse<GetMessageResult> getMessages(@RequestBody CommonRequest request) {
        String userId = EffectiveSubjectContext.require().userId();
        List<EventMessage> messages = messageRepository.findGeneratedForUser(userId, 1);
        LocalDateTime pushedAt = LocalDateTime.now();
        messages.forEach(message -> messageRepository.markPushed(message.getMessageId(), pushedAt));
        return new CommonResponse<>(new GetMessageResult(messages.stream().map(this::toInfo).toList()));
    }

    @PostMapping("/api/v1/message/getnotify")
    public CommonResponse<GetNotifyResult> getNotify(@RequestBody CommonRequest request) {
        // ponytail: legacy DataService.GetNotify throws NotImplementedException; keep the migrated shell empty.
        EffectiveSubjectContext.require();
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

        @JsonProperty("Messages")
        public List<MessageInfo> getLegacyMessages() {
            return messages;
        }
    }

    @Data
    public static class GetNotifyResult {
        private final List<NotifyInfo> notifies;

        @JsonProperty("Notifies")
        public List<NotifyInfo> getLegacyNotifies() {
            return notifies;
        }
    }

    @Data
    public static class NotifyInfo {
        private int count;
        private String authNo;

        @JsonProperty("Count")
        public int getLegacyCount() {
            return count;
        }

        @JsonProperty("AuthNo")
        public String getLegacyAuthNo() {
            return authNo;
        }
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

        @JsonProperty("MessageID")
        public String getLegacyMessageID() {
            return messageID;
        }

        @JsonProperty("GernerationTime")
        public String getLegacyGernerationTime() {
            if (gernerationTime == null) {
                return null;
            }
            long millis = gernerationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return "/Date(" + millis + ")/";
        }

        @JsonProperty("MessageContent")
        public String getLegacyMessageContent() {
            return messageContent;
        }

        @JsonProperty("ResultView")
        public long getLegacyResultView() {
            return resultView;
        }

        @JsonProperty("ObjId")
        public String getLegacyObjId() {
            return objId;
        }

        @JsonProperty("ResultViewType")
        public String getLegacyResultViewType() {
            return resultViewType;
        }

        @JsonProperty("ResultKey")
        public String getLegacyResultKey() {
            return resultKey;
        }

        @JsonProperty("Read")
        public boolean getLegacyRead() {
            return read;
        }

        @JsonProperty("TimeOut")
        public boolean getLegacyTimeOut() {
            return timeOut;
        }

        @JsonProperty("ReadDateTime")
        public LocalDateTime getLegacyReadDateTime() {
            return readDateTime;
        }
    }
}
