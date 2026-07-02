package org.fool.framework.auth.foolframework.auth;

import org.fool.framework.dao.DaoService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class LegacyLoginFactoryTest {
    @Test
    public void matchesLegacyUnicodeMd5WithoutBytePadding() {
        assertEquals("19a2854144b63a8f7617a6f22519b12", LoginFactory.toMD5("admin"));
        assertEquals("b081dbe85e1ec3ffc3d4e7d022740cd", LoginFactory.toMD5("password"));
        assertEquals("ce1473cf80c6b3fda8e3dfc06adc315", LoginFactory.toMD5("abc"));
        assertEquals("d41d8cd98f0b24e980998ecf8427e", LoginFactory.toMD5(""));
    }

    @Test
    public void loginLooksUpLoginNameAndReturnsPlainPasswordOnMatch() {
        User user = user("alice", "secret");
        RecordingDaoService dao = new RecordingDaoService(user);

        User result = new LoginFactory(dao).login("alice", "secret");

        assertSame(user, result);
        assertEquals("secret", result.getPassword());
        assertEquals("SELECT * FROM `SW_AUTH_USER` WHERE `USER_LOGINNAME` = ?", dao.lastSql);
        assertEquals("alice", dao.lastArgs[0]);
    }

    @Test
    public void loginReturnsNullWhenPasswordDoesNotMatch() {
        RecordingDaoService dao = new RecordingDaoService(user("alice", "secret"));

        assertNull(new LoginFactory(dao).login("alice", "wrong"));
    }

    @Test
    public void regUserPersistsLegacyPasswordHash() {
        RecordingDaoService dao = new RecordingDaoService();
        User user = user("bob", "plain");
        user.setPassword("plain");

        new LoginFactory(dao).regUser(user);

        assertSame(user, dao.created);
        assertEquals(LoginFactory.toMD5("plain"), user.getPassword());
    }

    @Test
    public void changePassWordChecksOldHashAndSavesNewHash() {
        User user = user("alice", "old");
        RecordingDaoService dao = new RecordingDaoService(user);

        assertTrue(new LoginFactory(dao).changePassWord("alice", "old", "new"));

        assertSame(user, dao.saved);
        assertEquals(LoginFactory.toMD5("new"), user.getPassword());
    }

    @Test
    public void changePassWordReturnsFalseWhenOldPasswordDoesNotMatch() {
        RecordingDaoService dao = new RecordingDaoService(user("alice", "old"));

        assertFalse(new LoginFactory(dao).changePassWord("alice", "wrong", "new"));
        assertNull(dao.saved);
    }

    @Test
    public void updateUserDelegatesToDaoSave() {
        RecordingDaoService dao = new RecordingDaoService();
        User user = user("alice", "secret");

        assertTrue(new LoginFactory(dao).updateUser(user));
        assertSame(user, dao.saved);
    }

    private static User user(String loginName, String password) {
        User user = new User();
        user.setLoginName(loginName);
        user.setPassword(LoginFactory.toMD5(password));
        return user;
    }

    private static final class RecordingDaoService extends DaoService {
        private final List<User> users = new ArrayList<>();
        private Object created;
        private Object saved;
        private String lastSql;
        private Object[] lastArgs;

        private RecordingDaoService(User... users) {
            this.users.addAll(List.of(users));
        }

        @Override
        public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {
            lastSql = sql;
            lastArgs = args;
            if (!User.class.equals(clazz)) {
                return List.of();
            }
            return users.stream()
                    .filter(user -> user.getLoginName().equals(args[0]))
                    .map(clazz::cast)
                    .toList();
        }

        @Override
        public <T> void create(T object) {
            created = object;
        }

        @Override
        public <T> boolean save(T object) {
            saved = object;
            return true;
        }
    }
}
