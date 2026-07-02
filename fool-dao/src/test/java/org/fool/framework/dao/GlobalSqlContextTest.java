package org.fool.framework.dao;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GlobalSqlContextTest {
    @Test
    public void returnsDefaultConnectionWhenTypeIsNotRegistered() {
        GlobalSqlContext.setConStr("jdbc:mysql://default");

        assertEquals("jdbc:mysql://default", GlobalSqlContext.getConStr(UnregisteredModel.class, "main"));
    }

    @Test
    public void registeredTypeConnectionOverridesDefaultAndIgnoresKeyName() {
        GlobalSqlContext.setConStr("jdbc:mysql://default");
        GlobalSqlContext.regSqlCon(RegisteredModel.class, "first", "jdbc:mysql://registered");

        assertEquals("jdbc:mysql://registered", GlobalSqlContext.getConStr(RegisteredModel.class, "other"));
    }

    @Test
    public void secondRegistrationForTypeReplacesFirstConnection() {
        GlobalSqlContext.regSqlCon(ReRegisteredModel.class, "first", "jdbc:mysql://old");
        GlobalSqlContext.regSqlCon(ReRegisteredModel.class, "second", "jdbc:mysql://new");

        assertEquals("jdbc:mysql://new", GlobalSqlContext.getConStr(ReRegisteredModel.class, "first"));
    }

    private static class UnregisteredModel {
    }

    private static class RegisteredModel {
    }

    private static class ReRegisteredModel {
    }
}
