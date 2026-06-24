package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventMakeService {
    public static final long DEFAULT_SLEEP_MILLIS = 60_000L;

    private final EventApplicationCatalog applicationCatalog;
    private final ScopedEventRuntime scopedRuntime;
    private final long sleepMillis;
    private final Sleeper sleeper;
    private volatile boolean running;
    private Thread thread;

    @Autowired
    public EventMakeService(EventApplicationCatalog applicationCatalog, ScopedEventRuntime scopedRuntime) {
        this(applicationCatalog, scopedRuntime, DEFAULT_SLEEP_MILLIS, Thread::sleep);
    }

    EventMakeService(
            EventApplicationCatalog applicationCatalog,
            ScopedEventRuntime scopedRuntime,
            long sleepMillis,
            Sleeper sleeper) {
        this.applicationCatalog = applicationCatalog;
        this.scopedRuntime = scopedRuntime;
        this.sleepMillis = sleepMillis;
        this.sleeper = sleeper;
    }

    public EventRuntimeResult workOnce() {
        EventRuntimeResult result = new EventRuntimeResult();
        try {
            for (EventApplicationScope application : applicationCatalog.findApplications()) {
                for (String databaseConnection : application.databaseConnections()) {
                    result.merge(scopedRuntime.process(application, databaseConnection));
                }
            }
        } catch (RuntimeException ex) {
            // Legacy EventMakeService swallowed one cycle's exceptions before sleeping.
        }
        return result;
    }

    public void work() {
        running = true;
        while (running) {
            workOnce();
            sleep();
        }
    }

    public void start() {
        thread = new Thread(this::work, "fool-event-make-service");
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread == null) {
            return;
        }
        try {
            thread.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    private void sleep() {
        try {
            sleeper.sleep(sleepMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long sleepMillis) throws InterruptedException;
    }
}
