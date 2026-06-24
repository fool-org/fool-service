package org.fool.framework.event;

import java.util.UUID;

public interface EventRecordRepository {
    boolean exists(UUID definitionId, String objectId);

    void save(EventRecord event);
}
