package org.fool.framework.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record EventMatchedObject(String objectId, Map<String, Object> values) {
    public EventMatchedObject {
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values == null ? Map.of() : values));
    }

    public EventMatchedObject(String objectId) {
        this(objectId, Map.of());
    }
}
