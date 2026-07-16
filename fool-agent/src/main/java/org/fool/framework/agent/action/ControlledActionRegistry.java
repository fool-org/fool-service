package org.fool.framework.agent.action;

import org.fool.framework.common.authz.ControlledActionException;
import org.fool.framework.common.authz.ControlledActionHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ControlledActionRegistry {
    private final Map<String, ControlledActionHandler> handlers;

    public ControlledActionRegistry(List<ControlledActionHandler> handlers) {
        Map<String, ControlledActionHandler> registered = new HashMap<>();
        for (ControlledActionHandler handler : handlers) {
            String key = key(handler.action(), handler.resourceType());
            if (registered.putIfAbsent(key, handler) != null) {
                throw new IllegalStateException("Duplicate controlled action handler: " + key);
            }
        }
        this.handlers = Map.copyOf(registered);
    }

    public ControlledActionHandler require(String action, String resourceType) {
        ControlledActionHandler handler = handlers.get(key(action, resourceType));
        if (handler == null) {
            throw new ControlledActionException(403, "ACTION_HANDLER_NOT_REGISTERED");
        }
        return handler;
    }

    public Set<String> registeredKeys() {
        return handlers.keySet();
    }

    static String key(String action, String resourceType) {
        return action + "|" + resourceType.toLowerCase(java.util.Locale.ROOT);
    }
}
