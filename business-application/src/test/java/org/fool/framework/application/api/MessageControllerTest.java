package org.fool.framework.application.api;

import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.event.EventMessage;
import org.fool.framework.event.EventMessageRepository;
import org.fool.framework.event.MsgState;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MessageControllerTest {
    @Test
    public void getmsgReturnsCurrentUserGeneratedMessagesAndMarksThemPushed() {
        UUID messageId = UUID.randomUUID();
        LocalDateTime generated = LocalDateTime.of(2026, 7, 3, 10, 20);
        EventMessage message = new EventMessage();
        message.setMessageId(messageId);
        message.setViewId("100");
        message.setObjectId("1001");
        message.setMessageFormat("Order timeout");
        message.setGenerateTime(generated);
        message.setState(MsgState.Generate);
        CapturingMessageRepository repository = new CapturingMessageRepository(List.of(message));
        MessageController controller = new MessageController(new StubAuthService("admin"), repository);
        CommonRequest request = new CommonRequest();
        request.setToken("token-1");

        CommonResponse<MessageController.GetMessageResult> response = controller.getMessages(request);

        assertEquals(0, response.getCode());
        assertEquals(List.of("admin"), repository.polledUsers);
        assertEquals(1, repository.pollLimits.get(0).intValue());
        assertEquals(List.of(messageId), repository.pushedMessages);
        MessageController.MessageInfo info = response.getData().getMessages().get(0);
        assertEquals(messageId.toString(), info.getMessageID());
        assertEquals(generated, info.getGernerationTime());
        assertEquals("Order timeout", info.getMessageContent());
        assertEquals(100L, info.getResultView());
        assertEquals("1001", info.getResultKey());
        assertFalse(info.isRead());
        assertFalse(info.isTimeOut());
    }

    @Test
    public void getnotifyReturnsEmptyLegacyNotifyList() {
        StubAuthService authService = new StubAuthService("admin");
        MessageController controller = new MessageController(authService, new CapturingMessageRepository(List.of()));
        CommonRequest request = new CommonRequest();
        request.setToken("token-1");

        CommonResponse<MessageController.GetNotifyResult> response = controller.getNotify(request);

        assertEquals(0, response.getCode());
        assertEquals(List.of("token-1"), authService.tokens);
        assertEquals(0, response.getData().getNotifies().size());
    }

    private static final class StubAuthService extends AuthService {
        private final String userId;
        private final List<String> tokens = new ArrayList<>();

        private StubAuthService(String userId) {
            this.userId = userId;
        }

        @Override
        public UserDTO getInfoByToken(String token) {
            tokens.add(token);
            UserDTO user = new UserDTO();
            user.setId(userId);
            return user;
        }
    }

    private static final class CapturingMessageRepository implements EventMessageRepository {
        private final List<EventMessage> messages;
        private final List<String> polledUsers = new ArrayList<>();
        private final List<Integer> pollLimits = new ArrayList<>();
        private final List<UUID> pushedMessages = new ArrayList<>();

        private CapturingMessageRepository(List<EventMessage> messages) {
            this.messages = messages;
        }

        @Override
        public void saveAll(List<EventMessage> messages) {
        }

        @Override
        public List<EventMessage> findGeneratedForUser(String userId, int limit) {
            polledUsers.add(userId);
            pollLimits.add(limit);
            return messages;
        }

        @Override
        public void markPushed(UUID messageId, LocalDateTime pushTime) {
            pushedMessages.add(messageId);
        }
    }
}
