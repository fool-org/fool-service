package org.fool.framework.dbmanage.action;

import org.fool.framework.common.authz.ControlledActionContext;
import org.fool.framework.common.authz.ControlledActionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Component
class DataSourceActionSupport {
    private final JdbcTemplate jdbcTemplate;

    DataSourceActionSupport(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    RoutePlan routePlan(ControlledActionContext context) {
        String key = key(context);
        String target = text(context.arguments().get("databaseNo"));
        if (!target.matches("[A-Za-z0-9._-]{1,32}")) throw denied("DATABASE_ROUTE_INVALID");
        String current = jdbcTemplate.query("SELECT `DS_DBNo` FROM `DS_DataSourceSet` WHERE `DS_Key` = ?",
                rs -> rs.next() ? rs.getString(1) : null, key);
        if (current == null) throw denied("RESOURCE_OUT_OF_SCOPE");
        Integer valid = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM `WorkDataBase` working
                  JOIN `DB_AppDB` link ON link.`DBNo` = working.`DBNo`
                  JOIN `DB_App` app ON app.`BO_Id` = link.`App_Id`
                 WHERE working.`DBNo` = ? AND working.`IsActive` = 1 AND app.`BO_AppName` = ?
                """, Integer.class, target, context.subject().appId());
        if (valid == null || valid != 1) throw denied("DATABASE_ROUTE_INVALID");
        return new RoutePlan(key, current, target, hash(key + "\u0000" + current));
    }

    void updateRoute(ControlledActionContext context) {
        RoutePlan plan = routePlan(context);
        requireApprovedSnapshot(context, plan.version());
        if (jdbcTemplate.update("UPDATE `DS_DataSourceSet` SET `DS_DBNo` = ? WHERE `DS_Key` = ? AND `DS_DBNo` = ?",
                plan.target(), plan.key(), plan.current()) != 1) {
            throw new ControlledActionException(409, "OBJECT_CHANGED");
        }
    }

    CredentialPlan credentialPlan(ControlledActionContext context) {
        String key = key(context);
        String reference = text(context.arguments().get("credentialRef"));
        if (!reference.matches("(?:vault|env):[A-Za-z0-9._/-]{1,240}")) {
            throw denied("CREDENTIAL_REFERENCE_INVALID");
        }
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `DS_DataSourceSet` WHERE `DS_Key` = ?", Integer.class, key);
        if (exists == null || exists != 1) throw denied("RESOURCE_OUT_OF_SCOPE");
        List<String> current = jdbcTemplate.queryForList("""
                SELECT `CREDENTIAL_REF` FROM `FOOL_DATASOURCE_CREDENTIAL_REF` WHERE `DS_KEY` = ?
                """, String.class, key);
        String previous = current.isEmpty() ? "" : current.get(0);
        return new CredentialPlan(key, reference, hash(key + "\u0000" + previous), !previous.isEmpty());
    }

    void updateCredential(ControlledActionContext context) {
        CredentialPlan plan = credentialPlan(context);
        requireApprovedSnapshot(context, plan.version());
        jdbcTemplate.update("""
                INSERT INTO `FOOL_DATASOURCE_CREDENTIAL_REF`
                  (`DS_KEY`, `CREDENTIAL_REF`, `UPDATED_BY`, `UPDATED_AT`)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP(6))
                ON DUPLICATE KEY UPDATE `CREDENTIAL_REF` = VALUES(`CREDENTIAL_REF`),
                  `UPDATED_BY` = VALUES(`UPDATED_BY`), `UPDATED_AT` = VALUES(`UPDATED_AT`)
                """, plan.key(), plan.reference(), context.subject().userId());
    }

    private String key(ControlledActionContext context) {
        String key = context.resourceId();
        if (key == null || !key.matches("[A-Za-z0-9._:-]{1,128}")) throw denied("RESOURCE_OUT_OF_SCOPE");
        return key;
    }

    private static void requireApprovedSnapshot(ControlledActionContext context, String current) {
        Object expected = context.arguments().get("_approvedSnapshotVersion");
        if (!(expected instanceof String value) || !value.equals(current)) {
            throw new ControlledActionException(409, "OBJECT_CHANGED");
        }
    }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String text(Object value) { return value == null ? "" : String.valueOf(value).trim(); }
    private static ControlledActionException denied(String reason) { return new ControlledActionException(400, reason); }

    record RoutePlan(String key, String current, String target, String version) {}
    record CredentialPlan(String key, String reference, String version, boolean replacesExisting) {}
}
