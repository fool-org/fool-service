package org.fool.framework.auth.foolframework.auth;

import org.fool.framework.dao.DaoService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class LegacyAuthoriezedFactoryTest {
    @Test
    public void getsAuthorizedUserDetailByLegacyUserId() {
        Object app = new Object();
        Object conFac = new Object();
        FakeDaoService daoService = new FakeDaoService();
        User user = new User();
        user.setUserId(42L);

        AuthoriezedFactory factory = new AuthoriezedFactory(app, conFac, daoService);

        assertSame(daoService.result, factory.getAuthrizedUser(user));
        assertEquals(AuthorizedUser.class, daoService.clazz);
        assertEquals(42L, daoService.key);
        assertSame(app, factory.getApp());
        assertSame(conFac, factory.getConFac());
    }

    private static final class FakeDaoService extends DaoService {
        private final AuthorizedUser result = new AuthorizedUser();
        private Class<?> clazz;
        private Object key;

        @Override
        public <T> T getOneDetailByKey(Class<T> clazz, Object key) {
            this.clazz = clazz;
            this.key = key;
            return clazz.cast(result);
        }
    }
}
