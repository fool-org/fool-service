package org.fool.framework.event;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class EventSchedulerLifecycle implements SmartLifecycle {
    private final EventMakeService eventMakeService;
    private final EventSchedulerProperties properties;
    private volatile boolean running;

    public EventSchedulerLifecycle(EventMakeService eventMakeService, EventSchedulerProperties properties) {
        this.eventMakeService = eventMakeService;
        this.properties = properties;
    }

    @Override
    public void start() {
        if (!properties.isEnabled() || running) {
            return;
        }
        eventMakeService.start();
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        eventMakeService.stop();
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
