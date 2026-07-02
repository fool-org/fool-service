package org.fool.framework.auth.foolframework.auth;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LegacyUserFactoryTest {
    @Test
    public void storesApplicationPayloadAndReturnsEmptyUserLists() throws Exception {
        Object app = new Object();
        UserFactory factory = new UserFactory(app);
        List<User> first = factory.getUsers();
        List<User> second = factory.getUsers();

        assertSame(app, app(factory));
        assertTrue(first.isEmpty());
        assertTrue(second.isEmpty());
        assertNotSame(first, second);
    }

    private static Object app(UserFactory factory) throws Exception {
        Field field = UserFactory.class.getDeclaredField("app");
        field.setAccessible(true);
        return field.get(factory);
    }
}
