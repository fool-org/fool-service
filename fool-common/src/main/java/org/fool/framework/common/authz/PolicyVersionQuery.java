package org.fool.framework.common.authz;

/**
 * A version fingerprint used as the authorization cache key and as an immutable
 * Action Request binding. Policy writes update UPDATED_AT, so stale subjects are
 * rejected without waiting for a TTL.
 */
public final class PolicyVersionQuery {
    public static final String SQL = """
            SELECT COALESCE(MAX(`VERSION_VALUE`), 0)
              FROM (
                    SELECT `POLICY_VERSION` AS `VERSION_VALUE`
                      FROM `FOOL_AUTHZ_POLICY_VERSION`
                     WHERE (`APP_ID` = ? OR `APP_ID` = '*')
                       AND (`DATABASE_ID` = ? OR `DATABASE_ID` = '*')
                    UNION ALL
                    SELECT TIMESTAMPDIFF(MICROSECOND, '1970-01-01 00:00:00', `UPDATED_AT`)
                      FROM `FOOL_AUTHZ_BINDING`
                     WHERE (`APP_ID` = ? OR `APP_ID` = '*')
                       AND (`DATABASE_ID` = ? OR `DATABASE_ID` = '*')
                    UNION ALL
                    SELECT TIMESTAMPDIFF(MICROSECOND, '1970-01-01 00:00:00', `UPDATED_AT`)
                      FROM `FOOL_AUTHZ_PERMISSION`
                    UNION ALL
                    SELECT TIMESTAMPDIFF(MICROSECOND, '1970-01-01 00:00:00', `UPDATED_AT`)
                      FROM `FOOL_AUTHZ_DATA_POLICY`
                   ) `POLICY_CHANGES`
            """;

    private PolicyVersionQuery() {
    }
}
