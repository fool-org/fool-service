package org.fool.framework.auth.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public class DepartmentTreeResolver {
    private final JdbcTemplate jdbcTemplate;

    public DepartmentTreeResolver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> expand(List<String> directDepartmentIds) {
        if (directDepartmentIds == null || directDepartmentIds.isEmpty()) {
            return List.of();
        }
        return expand(directDepartmentIds, jdbcTemplate.queryForList("""
                SELECT CAST(`SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID` AS CHAR) AS `PARENT_ID`,
                       CAST(`SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM` AS CHAR) AS `CHILD_ID`
                  FROM `SW_APP_AUTH_DEPARTMENT_SubDepartments`
                """));
    }

    static List<String> expand(List<String> directDepartmentIds, List<Map<String, Object>> relations) {
        Map<String, List<String>> children = new LinkedHashMap<>();
        for (Map<String, Object> relation : relations) {
            String parent = text(relation.get("PARENT_ID"));
            String child = text(relation.get("CHILD_ID"));
            if (!parent.isBlank() && !child.isBlank()) {
                children.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(child);
            }
        }
        LinkedHashSet<String> result = new LinkedHashSet<>(directDepartmentIds);
        ArrayDeque<String> pending = new ArrayDeque<>(result);
        while (!pending.isEmpty()) {
            for (String child : children.getOrDefault(pending.removeFirst(), List.of())) {
                if (result.add(child)) {
                    pending.addLast(child);
                }
            }
        }
        return List.copyOf(result);
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
