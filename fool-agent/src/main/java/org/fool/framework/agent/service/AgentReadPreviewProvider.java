package org.fool.framework.agent.service;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface AgentReadPreviewProvider {
    ReadPreview preview(String viewId, int pageSize);

    record ReadPreview(String status,
                       String reason,
                       long totalRecords,
                       List<String> columns,
                       List<Map<String, Object>> rows) {
        public ReadPreview {
            columns = columns == null ? List.of() : List.copyOf(columns);
            rows = rows == null ? List.of() : List.copyOf(rows);
        }

        public static ReadPreview unavailable(String reason) {
            return new ReadPreview("unavailable", reason, 0, List.of(), List.of());
        }

        public boolean passed() {
            return "passed".equals(status);
        }
    }
}
