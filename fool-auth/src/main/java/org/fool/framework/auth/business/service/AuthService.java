package org.fool.framework.auth.business.service;

import org.fool.framework.auth.business.common.BusinessErrorCode;
import org.fool.framework.auth.business.model.Auth;
import org.fool.framework.auth.business.model.User;
import org.fool.framework.auth.foolframework.auth.MenuItem;
import org.fool.framework.auth.dto.LoginVo;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.common.data.tree.ITreeFactory;
import org.fool.framework.common.data.tree.TreeNode;
import org.fool.framework.common.data.tree.TreeNodeCompareResult;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dto.CommonException;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
@Slf4j
public class AuthService {
    private static final String LEGACY_MENU_BASE_SQL = """
            select distinct m.* from SW_APP_AUTH_MENU m
            join SW_APP_AUTH_MENU_SW_APP_AUTH_ROLE mr on mr.SW_APP_AUTH_MENU_ID = m.AUTH_MENU_ID
            join SW_APP_AUTH_ROLE_SW_APP_AUTH_USER ru on ru.SW_APP_AUTH_ROLE_ID = mr.SW_APP_AUTH_ROLE_ID
            join SW_APP_AUTH_USER u on u.APP_AUTH_ID = ru.SW_APP_AUTH_USER_ID
            """;
    private static final String LEGACY_TOP_MENU_SQL = LEGACY_MENU_BASE_SQL + """
            where u.APP_AUTH_USERLOGINNAME = ?
              and not exists (
                select 1 from SW_APP_AUTH_MENU_SubItems s
                where s.SW_APP_AUTH_MENU_SUBITEMS_ITEM = m.AUTH_MENU_ID
              )
            order by m.AUTH_MENU_INDEX
            """;
    private static final String LEGACY_CHILD_MENU_SQL = LEGACY_MENU_BASE_SQL + """
            join SW_APP_AUTH_MENU_SubItems s on s.SW_APP_AUTH_MENU_SUBITEMS_ITEM = m.AUTH_MENU_ID
            where u.APP_AUTH_USERLOGINNAME = ?
              and s.SW_APP_AUTH_MENU_SubItemsAUTH_MENU_ID = ?
            order by m.AUTH_MENU_INDEX
            """;


    @Autowired
    private DaoService daoService;

    @Autowired
    private TokenService tokenService;


    /**
     * 登录
     *
     * @param id
     * @param password
     */
    public LoginVo login(String id, String password) {
        LoginVo loginVo = new LoginVo();
        User user = daoService.getOneByKey(User.class, id);
        if (user == null) {
            throw new CommonException(BusinessErrorCode.USER_NOT_FOUND, "用户不存在");
        } else {
            try {
                String info = passwordHash(user.getId(), password);
                if (!user.getPassword().equals(info)) {
                    throw new CommonException(BusinessErrorCode.PASSWORD_WRONG, "密码不正确");
                }
                loginVo.setUser(new UserDTO());
                BeanUtils.copyProperties(user, loginVo.getUser());
                loginVo.setToken(tokenService.getTokenByUid(id));
                return loginVo;
            } catch (NoSuchAlgorithmException e) {
                throw new CommonException(BusinessErrorCode.SYSTEM_ERROR, "发生系统错误");
            }
        }

    }

    /**
     * 注册
     * @param id
     * @param password
     * @param name
     * @param mobile
     * @return
     */
    public User register(String id, String password, String name, String mobile) {
        try {
            User user = new User();
            user.setId(id);
            user.setName(name);
            user.setMobile(mobile);
            user.setPassword(passwordHash(id, password));
            daoService.create(user);
            return user;
        } catch (Exception ex) {
            log.info("", ex);

        }
        return null;
    }

    String passwordHash(String id, String password) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        return HexFormat.of().formatHex(
                md5.digest((String.valueOf(id) + String.valueOf(password)).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 得到权限
     *
     * @param token
     */
    public List<TreeNode<Auth>> getAuth(String token) {
        String userId = tokenService.getUidByToken(token);
        String sql = " select distinct a.id,a.name ,a.auth_type, a.auth_name from auth_item a where a.id in (select auth_id from auth_role_auth where role_id in (select role_id from auth_user_role where user_id = ?)) order by a.id";
        return new ITreeFactory<Auth>().createTreeByLevel(daoService.selectList(Auth.class, sql, userId),
                (child, parent) -> {
                    String childId = child.getId();
                    String parentId = parent.getId();
                    if (childId.equals(parentId)) {
                        return TreeNodeCompareResult.Equal;
                    }
                    if (childId.length() > parentId.length()) {
                        if (childId.indexOf(parentId) == 0) {
                            return TreeNodeCompareResult.Child;
                        } else {
                            return TreeNodeCompareResult.NextLevel;
                        }
                    }
                    if (childId.length() == parentId.length()) {
                        if (childId.compareTo(parentId) < 0) {
                            return TreeNodeCompareResult.PreNode;
                        }
                        return TreeNodeCompareResult.NextNode;
                    } else {
                        return TreeNodeCompareResult.Parent;
                    }
                });
    }

    public List<LegacyAuthItem> getLegacySubMenus(String token, String parentAuthCode) {
        String userId = tokenService.getUidByToken(token);
        List<MenuItem> items = StringUtils.hasText(parentAuthCode)
                ? daoService.selectList(MenuItem.class, LEGACY_CHILD_MENU_SQL, userId, Long.parseLong(parentAuthCode))
                : daoService.selectList(MenuItem.class, LEGACY_TOP_MENU_SQL, userId);
        return items.stream().map(this::legacyAuthItem).toList();
    }

    private LegacyAuthItem legacyAuthItem(MenuItem menu) {
        LegacyAuthItem item = new LegacyAuthItem();
        item.setText(menu.getText());
        item.setNote(menu.getText());
        item.setImageUrl(menu.getImage());
        item.setAuthType(1);
        item.setViewId(menu.getViewId() == null ? 0L : menu.getViewId());
        item.setViewType(3);
        item.setIndex(menu.getIndex() == null ? 0 : menu.getIndex());
        item.setAuthNo(menu.getId() == null ? "" : menu.getId().toString());
        return item;
    }

    /**
     * @param token
     * @return
     */
    public UserDTO getInfoByToken(String token) {
        String userId = tokenService.getUidByToken(token);
        var dbuser = daoService.getOneByKey(User.class, userId);
        var user = new UserDTO();
        BeanUtils.copyProperties(dbuser, user);
        return user;
    }

    public void logout(String token) {
        tokenService.logoutToken(token);
    }

    @Data
    public static class LegacyAuthItem {
        private String text;
        private String note;
        private String imageUrl;
        private int authType;
        private long viewId;
        private int notifyCount;
        private int viewType;
        private int index;
        private String authNo;
    }
}
