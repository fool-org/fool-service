package org.fool.framework.auth.foolframework.auth;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class LegacyOrgFactoryTest {
    @Test
    public void companyFactoryReturnsEmptyUserLists() {
        CompanyFactory factory = new CompanyFactory();
        List<User> first = factory.getUsers(new Company());
        List<User> second = factory.getUsers(new Company());

        assertTrue(first.isEmpty());
        assertTrue(second.isEmpty());
        assertNotSame(first, second);
    }

    @Test
    public void departmentFactoryReturnsEmptyUserLists() {
        DepFactory factory = new DepFactory();
        List<User> first = factory.getUsers(new Department());
        List<User> second = factory.getUsers(new Department());

        assertTrue(first.isEmpty());
        assertTrue(second.isEmpty());
        assertNotSame(first, second);
    }
}
