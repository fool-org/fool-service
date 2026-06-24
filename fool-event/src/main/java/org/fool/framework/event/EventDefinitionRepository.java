package org.fool.framework.event;

import java.util.List;

public interface EventDefinitionRepository {
    List<EventDefinition> findRunningDefinitions();
}
