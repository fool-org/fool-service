package org.fool.framework.auth.foolframework.auth;

import org.fool.framework.dao.DaoService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

public class LoginFactory {
    private final DaoService daoService;

    public LoginFactory(DaoService daoService) {
        this.daoService = daoService;
    }

    public User login(String userName, String password) {
        User user = findByLoginName(userName);
        if (user == null || !Objects.equals(user.getPassword(), toMD5(password))) {
            return null;
        }
        user.setPassword(password);
        return user;
    }

    public void regUser(User user) {
        user.setPassword(toMD5(user.getPassword()));
        daoService.create(user);
    }

    public boolean changePassWord(String userName, String oldPwd, String newPwd) {
        User user = findByLoginName(userName);
        if (user == null || !Objects.equals(user.getPassword(), toMD5(oldPwd))) {
            return false;
        }
        user.setPassword(toMD5(newPwd));
        return daoService.save(user);
    }

    public boolean updateUser(User user) {
        return daoService.save(user);
    }

    public static String toMD5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_16LE));
            StringBuilder result = new StringBuilder();
            for (byte item : digest) {
                result.append(Integer.toHexString(item & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private User findByLoginName(String userName) {
        List<User> users = daoService.selectList(
                User.class,
                "SELECT * FROM `SW_AUTH_USER` WHERE `USER_LOGINNAME` = ?",
                userName);
        return users.isEmpty() ? null : users.get(0);
    }
}
