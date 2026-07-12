package org.fool.framework.auth.business.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.fool.framework.auth.business.common.BusinessErrorCode;
import org.fool.framework.auth.business.model.Auth;
import org.fool.framework.auth.business.model.User;
import org.fool.framework.auth.foolframework.auth.MenuItem;
import org.fool.framework.auth.dto.LoginVo;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.app.AppFacade;
import org.fool.framework.app.ApplicationDefinition;
import org.fool.framework.app.StoreDatabase;
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

    @Autowired
    private AppFacade appFacade;


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

    public String getLegacyUserAvatar(String token) {
        String userId = tokenService.getUidByToken(token);
        return daoService.selectList(
                        org.fool.framework.auth.foolframework.auth.User.class,
                        "select * from SW_AUTH_USER where USER_LOGINNAME = ?",
                        userId)
                .stream()
                .map(org.fool.framework.auth.foolframework.auth.User::getAvtar)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }

    public LegacyAppInfo getLegacyAppInfo(String token) {
        return legacyAppInfo(getLegacyApplication(token));
    }

    public void rememberLegacyApp(String token, String appId, String dbId) {
        tokenService.setLegacyAppId(token, appId);
        tokenService.setLegacyDbId(token, dbId);
    }

    public String getLegacyAppConnection(String token) {
        ApplicationDefinition app = getLegacyApplication(token);
        return app == null ? "" : empty(app.getSysCon());
    }

    public String getLegacyDataConnection(String token) {
        ApplicationDefinition app = getLegacyApplication(token);
        String dbId = tokenService.getLegacyDbId(token);
        if (app == null || !StringUtils.hasText(dbId)) {
            return "";
        }
        return getLegacyStoreDatabaseRows(app.getAppId()).stream()
                .filter(db -> dbId.equalsIgnoreCase(empty(db.getStoreBaseId())))
                .map(StoreDatabase::getConnection)
                .findFirst()
                .orElse("");
    }

    private ApplicationDefinition getLegacyApplication(String token) {
        tokenService.getUidByToken(token);
        String sessionAppId = tokenService.getLegacyAppId(token);
        List<ApplicationDefinition> apps = appFacade.getApps();
        return apps.stream()
                .filter(candidate -> StringUtils.hasText(sessionAppId)
                        && sessionAppId.equals(candidate.getAppId()))
                .findFirst()
                .orElseGet(() -> apps.stream().findFirst().orElse(null));
    }

    public LegacyAppInfo getLegacyAppInfo(String appId, String appKey) {
        ApplicationDefinition app = appFacade.getApp(appId, appKey);
        return app == null ? null : legacyAppInfo(app);
    }

    public LegacyInitAppInfo getLegacyInitAppInfo(String appId, String appKey) {
        ApplicationDefinition app = appFacade.getApp(appId, appKey);
        if (app == null) {
            return null;
        }
        LegacyInitAppInfo info = new LegacyInitAppInfo();
        info.setAppTitle(empty(app.getName()));
        info.setAppName(empty(app.getName()));
        info.setAppImg(empty(app.getInitImage()));
        info.setAppVersion(empty(app.getVersion()));
        info.setAppPowerBy(empty(app.getCompany()));
        info.setAppUrl(empty(app.getUrl()));
        info.setDbs(getLegacyStoreDatabases(appId));
        return info;
    }

    public boolean hasLegacyStoreDatabase(String appId, String dbId) {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(dbId)) {
            return false;
        }
        return getLegacyStoreDatabases(appId).stream()
                .anyMatch(db -> dbId.equalsIgnoreCase(db.getDbId()));
    }

    private List<LegacyStoreBaseInfo> getLegacyStoreDatabases(String appId) {
        return getLegacyStoreDatabaseRows(appId).stream()
                .map(this::legacyStoreBaseInfo)
                .toList();
    }

    private List<StoreDatabase> getLegacyStoreDatabaseRows(String appId) {
        String sql = """
                select db.* from SW_STOREDB db
                join SW_APPLICATION_SW_STOREDB rel on rel.SW_STOREDB_ID = db.SW_STORE_STOREID
                where rel.SW_APPLICATION_ID = ?
                """;
        return daoService.selectList(StoreDatabase.class, sql, appId);
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

    private LegacyAppInfo legacyAppInfo(ApplicationDefinition app) {
        LegacyAppInfo info = new LegacyAppInfo();
        if (app == null) {
            return info;
        }
        info.setAppName(empty(app.getName()));
        info.setAppVer(empty(app.getVersion()));
        info.setAppNote(empty(app.getNote()));
        info.setAppPowerBy(empty(app.getCompany()));
        info.setAppPowerUrl(empty(app.getUrl()));
        info.setAppLogoUrl(empty(app.getAvatar()));
        info.setDefaultViewId(app.getDefaultView() == null ? 0L : app.getDefaultView());
        info.setAppId(empty(app.getAppId()));
        return info;
    }

    private LegacyStoreBaseInfo legacyStoreBaseInfo(StoreDatabase db) {
        LegacyStoreBaseInfo info = new LegacyStoreBaseInfo();
        info.setDbId(empty(db.getStoreBaseId()));
        info.setDbName(empty(db.getName()));
        return info;
    }

    private static String empty(String value) {
        return value == null ? "" : value;
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

        @JsonProperty("Text")
        public String getLegacyText() {
            return text;
        }

        @JsonProperty("Note")
        public String getLegacyNote() {
            return note;
        }

        @JsonProperty("ImageUrl")
        public String getLegacyImageUrl() {
            return imageUrl;
        }

        @JsonProperty("AuthType")
        public int getLegacyAuthType() {
            return authType;
        }

        @JsonProperty("ViewId")
        public long getLegacyViewId() {
            return viewId;
        }

        @JsonProperty("NotifyCount")
        public int getLegacyNotifyCount() {
            return notifyCount;
        }

        @JsonProperty("ViewType")
        public int getLegacyViewType() {
            return viewType;
        }

        @JsonProperty("Index")
        public int getLegacyIndex() {
            return index;
        }

        @JsonProperty("AuthNo")
        public String getLegacyAuthNo() {
            return authNo;
        }
    }

    @Data
    public static class LegacyAppInfo {
        private String appName = "";
        private String appVer = "";
        private String appNote = "";
        private String appPowerBy = "";
        private String appPowerUrl = "";
        private String appLogoUrl = "";
        private long defaultViewId;
        private String appId = "";

        @JsonProperty("AppName")
        public String getLegacyAppName() {
            return appName;
        }

        @JsonProperty("AppVer")
        public String getLegacyAppVer() {
            return appVer;
        }

        @JsonProperty("AppNote")
        public String getLegacyAppNote() {
            return appNote;
        }

        @JsonProperty("AppPowerBy")
        public String getLegacyAppPowerBy() {
            return appPowerBy;
        }

        @JsonProperty("AppPowerUrl")
        public String getLegacyAppPowerUrl() {
            return appPowerUrl;
        }

        @JsonProperty("AppLogoUrl")
        public String getLegacyAppLogoUrl() {
            return appLogoUrl;
        }

        @JsonProperty("DefaultViewId")
        public long getLegacyDefaultViewId() {
            return defaultViewId;
        }

        @JsonProperty("AppId")
        public String getLegacyAppId() {
            return appId;
        }
    }

    @Data
    public static class LegacyInitAppInfo {
        private String appTitle = "";
        private String appName = "";
        private String appImg = "";
        private String appVersion = "";
        private String appPowerBy = "";
        private String appUrl = "";
        private List<LegacyStoreBaseInfo> dbs = List.of();
    }

    @Data
    public static class LegacyStoreBaseInfo {
        private String dbId = "";
        private String dbName = "";

        @JsonProperty("DbId")
        public String getLegacyDbId() {
            return dbId;
        }

        @JsonProperty("DbName")
        public String getLegacyDbName() {
            return dbName;
        }
    }
}
