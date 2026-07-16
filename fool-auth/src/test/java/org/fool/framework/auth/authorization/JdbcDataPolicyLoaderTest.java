package org.fool.framework.auth.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.common.authz.DataClassification;
import org.fool.framework.common.authz.DataPolicy;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JdbcDataPolicyLoaderTest {
    @Test
    public void mergesFieldsQuotasProvidersMasksAndClassificationsWithoutBroadening() {
        JdbcDataPolicyLoader loader = loader(Map.of(
                "one", row("[\"a\",\"b\"]", "[\"a\"]", "[\"a\"]", "[\"a\"]",
                        "{\"a\":\"LAST4\"}", 100, 80,
                        "[\"openai\",\"local\"]", "{\"a\":\"INTERNAL\"}"),
                "two", row("[\"c\"]", "[\"c\"]", "[\"c\"]", "[\"c\"]",
                        "{\"a\":\"FULL\"}", 50, 40,
                        "[\"local\"]", "{\"a\":\"CONFIDENTIAL\"}")));

        DataPolicy policy = loader.load(List.of("one", "two"));

        assertTrue(policy.filterable("a"));
        assertTrue(policy.filterable("c"));
        assertFalse(policy.filterable("b"));
        assertTrue(policy.sortable("a"));
        assertTrue(policy.exportable("c"));
        assertEquals(Integer.valueOf(50), policy.maxQueryRows());
        assertEquals(Integer.valueOf(40), policy.maxExportRows());
        assertEquals("FULL", policy.maskStrategy("a"));
        assertEquals(DataClassification.CONFIDENTIAL, policy.classification("a"));
        assertTrue(policy.providerAllowed("local", DataClassification.INTERNAL));
        assertFalse(policy.providerAllowed("openai", DataClassification.INTERNAL));
    }

    @Test
    public void disjointProviderAllowlistsDenyEveryProvider() {
        JdbcDataPolicyLoader loader = loader(Map.of(
                "one", row("[\"*\"]", "[]", "[]", "[]", "{}", 10, 10,
                        "[\"local\"]", "{}"),
                "two", row("[\"*\"]", "[]", "[]", "[]", "{}", 10, 10,
                        "[\"openai\"]", "{}")));

        DataPolicy policy = loader.load(List.of("one", "two"));

        assertFalse(policy.providerAllowed("local", DataClassification.INTERNAL));
        assertFalse(policy.providerAllowed("openai", DataClassification.INTERNAL));
    }

    @Test
    public void fieldDenyOverridesWildcardAllowAcrossRolesAndProviderIntersectionIgnoresCase() {
        JdbcDataPolicyLoader loader = loader(Map.of(
                "broad", row("[\"*\"]", "[\"*\"]", "[\"*\"]", "[\"*\"]",
                        "{}", 100, 100, "[\"OpenAI\",\"local\"]", "{}", "[]"),
                "restricted", row("[\"name\"]", "[\"name\"]", "[\"name\"]", "[\"name\"]",
                        "{}", 100, 100, "[\"openai\"]", "{}", "[\"secret\"]")));

        DataPolicy policy = loader.load(List.of("broad", "restricted"));

        assertTrue(policy.readable("name"));
        assertFalse(policy.readable("secret"));
        assertFalse(policy.filterable("SECRET"));
        assertFalse(policy.sortable("secret"));
        assertFalse(policy.exportable("secret"));
        assertFalse(policy.writable("secret"));
        assertFalse(policy.llmVisible("secret"));
        assertTrue(policy.providerAllowed("OPENAI", DataClassification.INTERNAL));
        assertFalse(policy.providerAllowed("local", DataClassification.INTERNAL));
    }

    private static JdbcDataPolicyLoader loader(Map<String, Map<String, Object>> rows) {
        JdbcTemplate jdbc = new JdbcTemplate() {
            @Override
            public List<Map<String, Object>> queryForList(String sql, Object... args) {
                Map<String, Object> row = rows.get(String.valueOf(args[0]));
                return row == null ? List.of() : List.of(row);
            }
        };
        return new JdbcDataPolicyLoader(jdbc, new ObjectMapper());
    }

    private static Map<String, Object> row(String readable,
                                           String filterable,
                                           String sortable,
                                           String exportable,
                                           String masks,
                                           int maxQuery,
                                           int maxExport,
                                           String providers,
                                           String classifications) {
        return row(readable, filterable, sortable, exportable, masks, maxQuery,
                maxExport, providers, classifications, "[]");
    }

    private static Map<String, Object> row(String readable,
                                           String filterable,
                                           String sortable,
                                           String exportable,
                                           String masks,
                                           int maxQuery,
                                           int maxExport,
                                           String providers,
                                           String classifications,
                                           String deniedFields) {
        String llm = "{\"filterableFields\":" + filterable
                + ",\"sortableFields\":" + sortable
                + ",\"exportableFields\":" + exportable
                + ",\"llmVisibleFields\":" + readable
                + ",\"allowedProviders\":" + providers
                + ",\"deniedFields\":" + deniedFields
                + ",\"classifications\":" + classifications + "}";
        return Map.of(
                "SCOPE_TYPE", "ALL",
                "FILTER_JSON", "{}",
                "READABLE_FIELDS_JSON", readable,
                "WRITABLE_FIELDS_JSON", readable,
                "MASK_FIELDS_JSON", masks,
                "MAX_QUERY_ROWS", maxQuery,
                "MAX_EXPORT_ROWS", maxExport,
                "LLM_POLICY_JSON", llm);
    }
}
