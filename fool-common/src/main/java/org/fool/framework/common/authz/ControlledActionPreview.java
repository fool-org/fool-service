package org.fool.framework.common.authz;

import java.util.List;
import java.util.Map;

public record ControlledActionPreview(String snapshotVersion,
                                      int affectedObjectCount,
                                      Map<String, Object> fieldDiff,
                                      List<String> preconditions,
                                      String rollbackStrategy,
                                      List<String> warnings,
                                      List<String> riskFactors) {
    public ControlledActionPreview(String snapshotVersion,
                                   int affectedObjectCount,
                                   Map<String, Object> fieldDiff,
                                   List<String> preconditions,
                                   String rollbackStrategy,
                                   List<String> warnings) {
        this(snapshotVersion, affectedObjectCount, fieldDiff, preconditions,
                rollbackStrategy, warnings, List.of());
    }

    public ControlledActionPreview {
        if (snapshotVersion == null || snapshotVersion.isBlank()) {
            throw new IllegalArgumentException("snapshotVersion is required.");
        }
        fieldDiff = fieldDiff == null ? Map.of() : Map.copyOf(fieldDiff);
        preconditions = preconditions == null ? List.of() : List.copyOf(preconditions);
        rollbackStrategy = rollbackStrategy == null ? "manual recovery" : rollbackStrategy;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        riskFactors = riskFactors == null ? List.of() : List.copyOf(riskFactors);
    }
}
