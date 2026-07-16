package org.fool.framework.agent.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ActionIntentParserTest {
    private final ActionIntentParser parser = new ActionIntentParser(new ObjectMapper());

    @Test
    public void acceptsOnlyCatalogActionAndStructuredArguments() {
        ActionIntent intent = parser.parse("""
                {
                  "schemaVersion": 1,
                  "action": "report.save",
                  "resource": {"type": "view", "id": "100"},
                  "arguments": {"name": "Daily report", "columns": ["symbol"]},
                  "rationale": "Save the reviewed report"
                }
                """);

        assertEquals("report.save", intent.action());
        assertEquals("view", intent.resource().type());
        assertEquals("100", intent.resource().id());
    }

    @Test
    public void rejectsServerOwnedFieldsAndExecutableValues() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"data.update","resource":{"type":"model","id":"100"},
                 "arguments":{"risk":"LOW"},"rationale":"test"}
                """));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"data.update","resource":{"type":"model","id":"100"},
                 "arguments":{"value":"UPDATE market_order SET state=1"},"rationale":"test"}
                """));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"data.update","resource":{"type":"model","id":"100"},
                 "arguments":{"value":"https://attacker.test"},"rationale":"test"}
                """));
    }

    @Test
    public void rejectsUnknownActionResourceAndRootFields() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"shell.exec","resource":{"type":"model","id":"100"},
                 "arguments":{},"rationale":"test"}
                """));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"model.configure","resource":{"type":"bean","id":"100"},
                 "arguments":{},"rationale":"test"}
                """));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"model.configure","resource":{"type":"model","id":"100"},
                 "arguments":{},"rationale":"test","actorUserId":"admin"}
                """));
    }

    @Test
    public void rejectsSecretsInNestedFieldsValuesAndRationaleButAllowsCredentialReferences() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"data.update","resource":{"type":"view","id":"100"},
                 "arguments":{"request":{"api_key":"secret-value"}},"rationale":"test"}
                """));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"data.update","resource":{"type":"view","id":"100"},
                 "arguments":{"value":"Bearer abcdefghijklmnopqrstuvwxyz"},"rationale":"test"}
                """));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("""
                {"schemaVersion":1,"action":"data.update","resource":{"type":"view","id":"100"},
                 "arguments":{"value":"safe"},"rationale":"password=hunter2"}
                """));

        ActionIntent reference = parser.parse("""
                {"schemaVersion":1,"action":"datasource.credential.update",
                 "resource":{"type":"datasource","id":"main"},
                 "arguments":{"credentialRef":"vault:database/fool-service"},"rationale":"rotate reference"}
                """);
        assertEquals("vault:database/fool-service", reference.arguments().get("credentialRef"));
    }
}
