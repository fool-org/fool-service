package org.fool.framework.event;

public interface ScopedEventRuntime {
    EventRuntimeResult process(EventApplicationScope application, String databaseConnection);
}
