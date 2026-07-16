package org.fool.framework.common.authz;

public final class EffectiveSubjectContext {
    private static final ThreadLocal<EffectiveSubject> CURRENT = new ThreadLocal<>();

    private EffectiveSubjectContext() {
    }

    public static void set(EffectiveSubject subject) {
        CURRENT.set(subject);
    }

    public static EffectiveSubject get() {
        return CURRENT.get();
    }

    public static EffectiveSubject require() {
        EffectiveSubject subject = CURRENT.get();
        if (subject == null) {
            throw new IllegalStateException("AUTHENTICATION_REQUIRED");
        }
        return subject;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
