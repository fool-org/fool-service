package org.fool.framework.model.service;

import org.fool.framework.model.model.DbMysqlDynamic;
import org.fool.framework.model.model.Model;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperationCommandValueResolverTest {
    @Test
    public void resolvesLegacyContextValuesThroughCallback() {
        Object value = new OperationCommandValueResolver().resolve(
                null,
                null,
                "@userid",
                (property, raw) -> raw,
                key -> "userid".equals(key) ? "admin" : "");

        assertEquals("admin", value);
    }

    @Test
    public void resolvesLegacyOwnerPropertyExpressions() {
        Model model = new Model();
        DbMysqlDynamic owner = new DbMysqlDynamic(model);
        owner.set("orderName", "parent order");
        DbMysqlDynamic child = new DbMysqlDynamic(model);
        child.setOwner(owner);

        Object value = new OperationCommandValueResolver().resolve(
                null,
                child,
                "#.orderName",
                (property, raw) -> raw);

        assertEquals("parent order", value);
    }
}
