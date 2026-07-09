package org.fool.framework.model.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbMysqlDynamicTest {
    @Test
    public void getIdFallsBackToLegacySysIdWhenModelHasNoIdProperty() {
        Model model = new Model();
        DbMysqlDynamic data = new DbMysqlDynamic(model);
        data.set("SYSID", "6101");

        assertEquals("6101", data.getId());
    }
}
