package org.fool.framework.common.authz;

@FunctionalInterface
public interface EffectiveSubjectLookup {
    EffectiveSubject resolve(String userId, String appId, String databaseId);

    static EffectiveSubjectLookup unavailable() {
        return (userId, appId, databaseId) -> {
            throw new ControlledActionException(503, "SUBJECT_LOOKUP_UNAVAILABLE");
        };
    }
}
