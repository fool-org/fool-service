package org.fool.framework.event;

import java.util.List;

public interface EventMessageRepository {
    void saveAll(List<EventMessage> messages);
}
