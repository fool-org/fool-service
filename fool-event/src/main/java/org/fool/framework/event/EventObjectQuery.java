package org.fool.framework.event;

import java.util.List;

public interface EventObjectQuery {
    List<EventMatchedObject> findMatchedObjects(EventDefinition definition);
}
