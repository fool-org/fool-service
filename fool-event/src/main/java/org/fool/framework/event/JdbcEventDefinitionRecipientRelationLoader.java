package org.fool.framework.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
public class JdbcEventDefinitionRecipientRelationLoader implements EventDefinitionRelationLoader {
    static final String DEFINITION_ID_COLUMN = "SW_EVT_DEF_ID";
    static final String COMPANY_DEPARTMENT_COMPANY_ID_COLUMN = "SW_APP_AUTH_COMPANY_DepsAPP_COR_ID";
    static final String COMPANY_ID_COLUMN = "APP_COR_ID";
    static final String DEPARTMENT_ID_COLUMN = "APP_DEP_ID";
    static final String DEPARTMENT_AUTH_USER_DEPARTMENT_ID_COLUMN = "APP_AUTH_DEP";
    static final String SUB_DEPARTMENT_PARENT_ID_COLUMN = "SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID";
    static final String ROLE_ID_COLUMN = "AUTH_ROLE_ID";
    static final String ROLE_RELATION_ID_COLUMN = "SW_APP_AUTH_ROLE_ID";
    static final String USER_ID_COLUMN = "APP_AUTH_USERID";
    static final String SELECT_NOTIFY_USERS_SQL_TEMPLATE = """
            SELECT rel.`SW_EVT_DEF_ID`, auth.`APP_AUTH_USERID`
            FROM `SW_APP_AUTH_USER_SW_EVT_DEF` rel
            JOIN `SW_APP_AUTH_USER` auth
              ON rel.`SW_APP_AUTH_USER_ID` = auth.`APP_AUTH_ID`
            WHERE rel.`SW_EVT_DEF_ID` IN (%s)
            ORDER BY rel.`SW_EVT_DEF_ID`, rel.`SW_APP_AUTH_USER_ID`
            """;
    static final String SELECT_NOTIFY_ROLES_SQL_TEMPLATE = """
            SELECT rel.`SW_EVT_DEF_ID`, role.`AUTH_ROLE_ID`
            FROM `SW_APP_AUTH_ROLE_SW_EVT_DEF` rel
            JOIN `SW_APP_AUTH_ROLE` role
              ON rel.`SW_APP_AUTH_ROLE_ID` = role.`AUTH_ROLE_ID`
            WHERE rel.`SW_EVT_DEF_ID` IN (%s)
            ORDER BY rel.`SW_EVT_DEF_ID`, rel.`SW_APP_AUTH_ROLE_ID`
            """;
    static final String SELECT_ROLE_AUTH_USERS_SQL_TEMPLATE = """
            SELECT role_user.`SW_APP_AUTH_ROLE_ID`, auth.`APP_AUTH_USERID`
            FROM `SW_APP_AUTH_ROLE_SW_APP_AUTH_USER` role_user
            JOIN `SW_APP_AUTH_USER` auth
              ON role_user.`SW_APP_AUTH_USER_ID` = auth.`APP_AUTH_ID`
            WHERE role_user.`SW_APP_AUTH_ROLE_ID` IN (%s)
            ORDER BY role_user.`SW_APP_AUTH_ROLE_ID`, role_user.`SW_APP_AUTH_USER_ID`
            """;
    static final String SELECT_NOTIFY_DEPARTMENTS_SQL_TEMPLATE = """
            SELECT rel.`SW_EVT_DEF_ID`, dep.`APP_DEP_ID`
            FROM `SW_APP_AUTH_DEPARTMENT_SW_EVT_DEF` rel
            JOIN `SW_APP_AUTH_DEPARTMENT` dep
              ON rel.`SW_APP_AUTH_DEPARTMENT_ID` = dep.`APP_DEP_ID`
            WHERE rel.`SW_EVT_DEF_ID` IN (%s)
            ORDER BY rel.`SW_EVT_DEF_ID`, rel.`SW_APP_AUTH_DEPARTMENT_ID`
            """;
    static final String SELECT_DEPARTMENT_AUTH_USERS_SQL_TEMPLATE = """
            SELECT auth.`APP_AUTH_DEP`, auth.`APP_AUTH_USERID`
            FROM `SW_APP_AUTH_USER` auth
            WHERE auth.`APP_AUTH_DEP` IN (%s)
            ORDER BY auth.`APP_AUTH_DEP`, auth.`APP_AUTH_ID`
            """;
    static final String SELECT_SUB_DEPARTMENTS_SQL_TEMPLATE = """
            SELECT rel.`SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID`, dep.`APP_DEP_ID`
            FROM `SW_APP_AUTH_DEPARTMENT_SubDepartments` rel
            JOIN `SW_APP_AUTH_DEPARTMENT` dep
              ON rel.`SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM` = dep.`APP_DEP_ID`
            WHERE rel.`SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID` IN (%s)
            ORDER BY rel.`SW_APP_AUTH_DEPARTMENT_SubDepartmentsAPP_DEP_ID`,
                     rel.`SW_APP_AUTH_DEPARTMENT_SUBDEPARTMENTS_ITEM`
            """;
    static final String SELECT_NOTIFY_COMPANIES_SQL_TEMPLATE = """
            SELECT rel.`SW_EVT_DEF_ID`, company.`APP_COR_ID`
            FROM `SW_APP_AUTH_COMPANY_SW_EVT_DEF` rel
            JOIN `SW_APP_AUTH_COMPANY` company
              ON rel.`SW_APP_AUTH_COMPANY_ID` = company.`APP_COR_ID`
            WHERE rel.`SW_EVT_DEF_ID` IN (%s)
            ORDER BY rel.`SW_EVT_DEF_ID`, rel.`SW_APP_AUTH_COMPANY_ID`
            """;
    static final String SELECT_COMPANY_DEPARTMENTS_SQL_TEMPLATE = """
            SELECT dep.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID`, dep.`APP_DEP_ID`
            FROM `SW_APP_AUTH_DEPARTMENT` dep
            WHERE dep.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID` IN (%s)
            ORDER BY dep.`SW_APP_AUTH_COMPANY_DepsAPP_COR_ID`, dep.`APP_DEP_ID`
            """;

    private final Function<List<UUID>, List<Map<String, Object>>> notifyUserRows;
    private final Function<List<UUID>, List<Map<String, Object>>> notifyRoleRows;
    private final Function<List<String>, List<Map<String, Object>>> roleAuthUserRows;
    private final Function<List<UUID>, List<Map<String, Object>>> notifyDepartmentRows;
    private final Function<List<String>, List<Map<String, Object>>> departmentAuthUserRows;
    private final Function<List<String>, List<Map<String, Object>>> subDepartmentRows;
    private final Function<List<UUID>, List<Map<String, Object>>> notifyCompanyRows;
    private final Function<List<String>, List<Map<String, Object>>> companyDepartmentRows;

    @Autowired
    public JdbcEventDefinitionRecipientRelationLoader(JdbcTemplate jdbcTemplate) {
        this(
                definitionIds -> queryForDefinitionRows(
                        jdbcTemplate, SELECT_NOTIFY_USERS_SQL_TEMPLATE, definitionIds),
                definitionIds -> queryForDefinitionRows(
                        jdbcTemplate, SELECT_NOTIFY_ROLES_SQL_TEMPLATE, definitionIds),
                roleIds -> queryForStringRows(
                        jdbcTemplate, SELECT_ROLE_AUTH_USERS_SQL_TEMPLATE, roleIds),
                definitionIds -> queryForDefinitionRows(
                        jdbcTemplate, SELECT_NOTIFY_DEPARTMENTS_SQL_TEMPLATE, definitionIds),
                departmentIds -> queryForStringRows(
                        jdbcTemplate, SELECT_DEPARTMENT_AUTH_USERS_SQL_TEMPLATE, departmentIds),
                departmentIds -> queryForStringRows(
                        jdbcTemplate, SELECT_SUB_DEPARTMENTS_SQL_TEMPLATE, departmentIds),
                definitionIds -> queryForDefinitionRows(
                        jdbcTemplate, SELECT_NOTIFY_COMPANIES_SQL_TEMPLATE, definitionIds),
                companyIds -> queryForStringRows(
                        jdbcTemplate, SELECT_COMPANY_DEPARTMENTS_SQL_TEMPLATE, companyIds));
    }

    JdbcEventDefinitionRecipientRelationLoader(Function<List<UUID>, List<Map<String, Object>>> notifyUserRows) {
        this(notifyUserRows, ids -> List.of(), ids -> List.of());
    }

    JdbcEventDefinitionRecipientRelationLoader(
            Function<List<UUID>, List<Map<String, Object>>> notifyUserRows,
            Function<List<UUID>, List<Map<String, Object>>> notifyRoleRows,
            Function<List<String>, List<Map<String, Object>>> roleAuthUserRows) {
        this(
                notifyUserRows,
                notifyRoleRows,
                roleAuthUserRows,
                ids -> List.of(),
                ids -> List.of(),
                ids -> List.of(),
                ids -> List.of(),
                ids -> List.of());
    }

    JdbcEventDefinitionRecipientRelationLoader(
            Function<List<UUID>, List<Map<String, Object>>> notifyUserRows,
            Function<List<UUID>, List<Map<String, Object>>> notifyRoleRows,
            Function<List<String>, List<Map<String, Object>>> roleAuthUserRows,
            Function<List<UUID>, List<Map<String, Object>>> notifyDepartmentRows,
            Function<List<String>, List<Map<String, Object>>> departmentAuthUserRows,
            Function<List<String>, List<Map<String, Object>>> subDepartmentRows,
            Function<List<UUID>, List<Map<String, Object>>> notifyCompanyRows,
            Function<List<String>, List<Map<String, Object>>> companyDepartmentRows) {
        this.notifyUserRows = notifyUserRows;
        this.notifyRoleRows = notifyRoleRows;
        this.roleAuthUserRows = roleAuthUserRows;
        this.notifyDepartmentRows = notifyDepartmentRows;
        this.departmentAuthUserRows = departmentAuthUserRows;
        this.subDepartmentRows = subDepartmentRows;
        this.notifyCompanyRows = notifyCompanyRows;
        this.companyDepartmentRows = companyDepartmentRows;
    }

    @Override
    public void loadRelations(List<EventDefinition> definitions) {
        Map<UUID, EventDefinition> definitionsById = definitionsById(definitions);
        List<UUID> definitionIds = new ArrayList<>(definitionsById.keySet());
        loadNotifyUsers(definitionsById, definitionIds);
        loadNotifyRoles(definitionsById, definitionIds);
        List<EventDepartment> departmentRoots = new ArrayList<>();
        loadNotifyDepartments(definitionsById, definitionIds, departmentRoots);
        loadNotifyCompanies(definitionsById, definitionIds, departmentRoots);
        expandDepartmentTrees(departmentRoots);
    }

    private void loadNotifyUsers(Map<UUID, EventDefinition> definitionsById, List<UUID> definitionIds) {
        for (Map<String, Object> row : notifyUserRows.apply(definitionIds)) {
            UUID definitionId = uuidOf(row.get(DEFINITION_ID_COLUMN));
            EventDefinition definition = definitionsById.get(definitionId);
            String userId = valueOf(row.get(USER_ID_COLUMN));
            if (definition != null && userId != null && !userId.isBlank()) {
                definition.getNotifyUsers().add(new EventRecipient(userId));
            }
        }
    }

    private void loadNotifyRoles(Map<UUID, EventDefinition> definitionsById, List<UUID> definitionIds) {
        Map<String, List<EventRole>> rolesById = new LinkedHashMap<>();
        for (Map<String, Object> row : notifyRoleRows.apply(definitionIds)) {
            UUID definitionId = uuidOf(row.get(DEFINITION_ID_COLUMN));
            EventDefinition definition = definitionsById.get(definitionId);
            String roleId = valueOf(row.get(ROLE_ID_COLUMN));
            if (definition != null && roleId != null && !roleId.isBlank()) {
                EventRole role = new EventRole(roleId);
                definition.getNotifyRoles().add(role);
                rolesById.computeIfAbsent(roleId, key -> new ArrayList<>()).add(role);
            }
        }
        if (rolesById.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : roleAuthUserRows.apply(new ArrayList<>(rolesById.keySet()))) {
            List<EventRole> roles = rolesById.get(valueOf(row.get(ROLE_RELATION_ID_COLUMN)));
            String userId = valueOf(row.get(USER_ID_COLUMN));
            if (roles != null && userId != null && !userId.isBlank()) {
                for (EventRole role : roles) {
                    role.getAuthUsers().add(new EventRecipient(userId));
                }
            }
        }
    }

    private void loadNotifyDepartments(
            Map<UUID, EventDefinition> definitionsById,
            List<UUID> definitionIds,
            List<EventDepartment> departmentRoots) {
        for (Map<String, Object> row : notifyDepartmentRows.apply(definitionIds)) {
            UUID definitionId = uuidOf(row.get(DEFINITION_ID_COLUMN));
            EventDefinition definition = definitionsById.get(definitionId);
            String departmentId = valueOf(row.get(DEPARTMENT_ID_COLUMN));
            if (definition != null && departmentId != null && !departmentId.isBlank()) {
                EventDepartment department = new EventDepartment(departmentId);
                definition.getNotifyDepartments().add(department);
                departmentRoots.add(department);
            }
        }
    }

    private void loadNotifyCompanies(
            Map<UUID, EventDefinition> definitionsById,
            List<UUID> definitionIds,
            List<EventDepartment> departmentRoots) {
        Map<String, List<EventCompany>> companiesById = new LinkedHashMap<>();
        for (Map<String, Object> row : notifyCompanyRows.apply(definitionIds)) {
            UUID definitionId = uuidOf(row.get(DEFINITION_ID_COLUMN));
            EventDefinition definition = definitionsById.get(definitionId);
            String companyId = valueOf(row.get(COMPANY_ID_COLUMN));
            if (definition != null && companyId != null && !companyId.isBlank()) {
                EventCompany company = new EventCompany(companyId);
                definition.getNotifyCompanies().add(company);
                companiesById.computeIfAbsent(companyId, key -> new ArrayList<>()).add(company);
            }
        }
        if (companiesById.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : companyDepartmentRows.apply(new ArrayList<>(companiesById.keySet()))) {
            List<EventCompany> companies = companiesById.get(valueOf(row.get(COMPANY_DEPARTMENT_COMPANY_ID_COLUMN)));
            String departmentId = valueOf(row.get(DEPARTMENT_ID_COLUMN));
            if (companies != null && departmentId != null && !departmentId.isBlank()) {
                for (EventCompany company : companies) {
                    EventDepartment department = new EventDepartment(departmentId);
                    company.getDepartments().add(department);
                    departmentRoots.add(department);
                }
            }
        }
    }

    private void expandDepartmentTrees(List<EventDepartment> departmentRoots) {
        if (departmentRoots.isEmpty()) {
            return;
        }
        Map<String, List<String>> childIdsByParentId = loadSubDepartmentClosure(departmentRoots);
        Map<String, List<EventDepartment>> departmentsById = new LinkedHashMap<>();
        materializeSubDepartments(departmentRoots, childIdsByParentId, departmentsById, List.of());
        loadDepartmentUsers(departmentsById);
    }

    private Map<String, List<String>> loadSubDepartmentClosure(List<EventDepartment> departmentRoots) {
        Set<String> knownDepartmentIds = departmentRoots.stream()
                .map(EventDepartment::getDepartmentId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> queriedDepartmentIds = new LinkedHashSet<>();
        Map<String, List<String>> childIdsByParentId = new LinkedHashMap<>();

        while (queriedDepartmentIds.size() < knownDepartmentIds.size()) {
            List<String> batch = knownDepartmentIds.stream()
                    .filter(id -> !queriedDepartmentIds.contains(id))
                    .toList();
            queriedDepartmentIds.addAll(batch);
            for (Map<String, Object> row : subDepartmentRows.apply(batch)) {
                String parentId = valueOf(row.get(SUB_DEPARTMENT_PARENT_ID_COLUMN));
                String childId = valueOf(row.get(DEPARTMENT_ID_COLUMN));
                if (parentId != null && !parentId.isBlank() && childId != null && !childId.isBlank()) {
                    childIdsByParentId.computeIfAbsent(parentId, key -> new ArrayList<>()).add(childId);
                    knownDepartmentIds.add(childId);
                }
            }
        }
        return childIdsByParentId;
    }

    private void materializeSubDepartments(
            List<EventDepartment> departments,
            Map<String, List<String>> childIdsByParentId,
            Map<String, List<EventDepartment>> departmentsById,
            List<String> path) {
        for (EventDepartment department : departments) {
            String departmentId = department.getDepartmentId();
            if (departmentId == null || departmentId.isBlank()) {
                continue;
            }
            departmentsById.computeIfAbsent(departmentId, key -> new ArrayList<>()).add(department);
            List<String> nextPath = new ArrayList<>(path);
            nextPath.add(departmentId);
            List<EventDepartment> children = new ArrayList<>();
            for (String childId : childIdsByParentId.getOrDefault(departmentId, List.of())) {
                if (!nextPath.contains(childId)) {
                    EventDepartment child = new EventDepartment(childId);
                    department.getSubDepartments().add(child);
                    children.add(child);
                }
            }
            materializeSubDepartments(children, childIdsByParentId, departmentsById, nextPath);
        }
    }

    private void loadDepartmentUsers(Map<String, List<EventDepartment>> departmentsById) {
        if (departmentsById.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : departmentAuthUserRows.apply(new ArrayList<>(departmentsById.keySet()))) {
            List<EventDepartment> departments = departmentsById.get(valueOf(row.get(DEPARTMENT_AUTH_USER_DEPARTMENT_ID_COLUMN)));
            String userId = valueOf(row.get(USER_ID_COLUMN));
            if (departments != null && userId != null && !userId.isBlank()) {
                for (EventDepartment department : departments) {
                    department.getUsers().add(new EventRecipient(userId));
                }
            }
        }
    }

    private static Map<UUID, EventDefinition> definitionsById(List<EventDefinition> definitions) {
        Map<UUID, EventDefinition> definitionsById = new LinkedHashMap<>();
        for (EventDefinition definition : definitions) {
            if (definition.getDefId() != null) {
                definitionsById.putIfAbsent(definition.getDefId(), definition);
            }
        }
        return definitionsById;
    }

    private static List<Map<String, Object>> queryForDefinitionRows(
            JdbcTemplate jdbcTemplate,
            String sqlTemplate,
            List<UUID> definitionIds) {
        if (definitionIds.isEmpty()) {
            return List.of();
        }
        String sql = String.format(sqlTemplate, placeholders(definitionIds.size()));
        Object[] args = definitionIds.stream().map(UUID::toString).toArray();
        return jdbcTemplate.queryForList(sql, args);
    }

    private static List<Map<String, Object>> queryForStringRows(
            JdbcTemplate jdbcTemplate,
            String sqlTemplate,
            List<String> values) {
        if (values.isEmpty()) {
            return List.of();
        }
        String sql = String.format(sqlTemplate, placeholders(values.size()));
        return jdbcTemplate.queryForList(sql, values.toArray());
    }

    private static String placeholders(int size) {
        return IntStream.range(0, size)
                .mapToObj(index -> "?")
                .collect(Collectors.joining(", "));
    }

    private static UUID uuidOf(Object value) {
        return value == null ? null : UUID.fromString(value.toString());
    }

    private static String valueOf(Object value) {
        return value == null ? null : value.toString();
    }
}
