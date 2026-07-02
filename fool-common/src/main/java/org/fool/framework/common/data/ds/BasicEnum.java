package org.fool.framework.common.data.ds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BasicEnum {
    private static final Map<Class<? extends BasicEnum>, Map<Integer, BasicEnum>> REGISTRY = new LinkedHashMap<>();

    private final int value;
    private final String state;

    protected BasicEnum(int value, String state, Class<? extends BasicEnum> type) {
        this.value = value;
        this.state = state;
        synchronized (REGISTRY) {
            Map<Integer, BasicEnum> values = REGISTRY.computeIfAbsent(type, key -> new LinkedHashMap<>());
            if (values.containsKey(value)) {
                throw new IllegalArgumentException("Duplicate BasicEnum value");
            }
            values.put(value, this);
        }
        // ponytail: skips legacy enum.txt debug dump; add only if compatibility depends on that file.
    }

    public int getValue() {
        return value;
    }

    public String getState() {
        return state;
    }

    public int intValue() {
        return value;
    }

    @Override
    public String toString() {
        return state;
    }

    protected static <T extends BasicEnum> T getBy(int value, Class<T> type) {
        synchronized (REGISTRY) {
            Map<Integer, BasicEnum> values = REGISTRY.get(type);
            if (values == null) {
                return null;
            }
            return type.cast(values.get(value));
        }
    }

    public static BasicEnum[] all(Class<? extends BasicEnum> type) {
        synchronized (REGISTRY) {
            Map<Integer, BasicEnum> values = REGISTRY.get(type);
            if (values == null) {
                return new BasicEnum[0];
            }
            List<BasicEnum> result = new ArrayList<>(values.values());
            return result.toArray(new BasicEnum[0]);
        }
    }
}
