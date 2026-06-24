package org.fool.framework.event;

import lombok.Getter;

@Getter
public class EventRuntimeResult {
    private int definitionsChecked;
    private int objectsMatched;
    private int eventsCreated;
    private int eventsSkipped;

    void definitionChecked() {
        definitionsChecked++;
    }

    void objectMatched() {
        objectsMatched++;
    }

    void eventCreated() {
        eventsCreated++;
    }

    void eventSkipped() {
        eventsSkipped++;
    }

    void merge(EventRuntimeResult other) {
        if (other == null) {
            return;
        }
        definitionsChecked += other.definitionsChecked;
        objectsMatched += other.objectsMatched;
        eventsCreated += other.eventsCreated;
        eventsSkipped += other.eventsSkipped;
    }
}
