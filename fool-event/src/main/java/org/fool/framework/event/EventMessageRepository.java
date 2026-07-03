package org.fool.framework.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EventMessageRepository {
    void saveAll(List<EventMessage> messages);

    List<EventMessage> findGeneratedForUser(String userId, int limit);

    void markPushed(UUID messageId, LocalDateTime pushTime);
}
