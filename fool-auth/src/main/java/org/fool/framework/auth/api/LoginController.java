package org.fool.framework.auth.api;


import com.fasterxml.jackson.annotation.JsonAlias;
import org.fool.framework.auth.business.model.Auth;
import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.auth.business.service.CheckCodeService;
import org.fool.framework.auth.dto.LoginDTO;
import org.fool.framework.auth.dto.LoginVo;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.common.data.tree.TreeNode;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/auth")
@RestController
@Api("用户登录相关接口")
public class LoginController {


    @Autowired
    private AuthService authService;
    @Autowired
    private CheckCodeService checkCodeService;

    @ApiOperation("登录")
    @PostMapping("/login")
    @ResponseBody
    public CommonResponse login(@RequestBody LoginDTO loginDTO) {
        var user = authService.login(loginDTO.getUserId(), loginDTO.getPassword());
        return new CommonResponse<LoginVo>(user);

    }

    @ApiOperation("旧版登录V2")
    @PostMapping("/loginv2")
    @ResponseBody
    public CommonResponse<LegacyLoginResult> loginV2(@RequestBody LegacyLoginRequest request) {
        CheckCodeService.CheckCodeRequest checkCode = new CheckCodeService.CheckCodeRequest(
                request.getCheckCodeKey(), request.getCheckCode(), null);
        if (!checkCodeService.validate(checkCode)) {
            return new CommonResponse<>(LegacyLoginResult.error(
                    10006, "Check code error."));
        }
        AuthService.LegacyAppInfo app = authService.getLegacyAppInfo(request.getAppId(), request.getAppKey());
        if (app == null) {
            return new CommonResponse<>(LegacyLoginResult.error(
                    10003, "Wrong application sec."));
        }
        if (!authService.hasLegacyStoreDatabase(request.getAppId(), request.getDbId())) {
            return new CommonResponse<>(LegacyLoginResult.error(
                    10007, "Unauthorized DataBase ."));
        }
        LoginVo login = authService.login(request.getUserId(), request.getPassWord());
        return new CommonResponse<>(LegacyLoginResult.success(
                login.getToken(), legacyUser(login.getUser()), app));
    }

    @ApiOperation("得到旧版验证码")
    @PostMapping("/getcheckcode")
    @ResponseBody
    public CommonResponse<CheckCodeService.CheckCodeResult> getCheckCode() {
        return new CommonResponse<>(checkCodeService.create());
    }

    @ApiOperation("校验旧版验证码")
    @PostMapping("/checkcode")
    @ResponseBody
    public CommonResponse<Boolean> checkCode(@RequestBody CheckCodeService.CheckCodeRequest request) {
        return new CommonResponse<>(checkCodeService.validate(request));
    }

    @ApiOperation("得到用户配置信息")
    @PostMapping("/profile")
    @ResponseBody
    public CommonResponse<UserDTO> getProfile(@RequestBody CommonRequest request) {
        var user = authService.getInfoByToken(request.getToken());
        return new CommonResponse<UserDTO>(user);
    }

    @ApiOperation("得到菜单信息")
    @PostMapping("/auth-menus")
    @ResponseBody
    public CommonResponse<List<TreeNode<Auth>>> getMenus(@RequestBody CommonRequest request) {
        return new CommonResponse<List<TreeNode<Auth>>>(authService.getAuth(request.getToken()));
    }

    @ApiOperation("得到旧版子菜单")
    @PostMapping("/getsubmenu")
    @ResponseBody
    public CommonResponse<LegacySubMenuResult> getSubMenu(@RequestBody LegacySubMenuRequest request) {
        return new CommonResponse<>(
                new LegacySubMenuResult(request.getToken(),
                        authService.getLegacySubMenus(request.getToken(), request.getParentAuthCode())));
    }

    @ApiOperation("得到旧版主界面信息")
    @PostMapping("/getmain")
    @ResponseBody
    public CommonResponse<LegacyMainResult> getMain(@RequestBody String token) {
        String normalizedToken = legacyRawToken(token);
        UserDTO user = authService.getInfoByToken(normalizedToken);
        return new CommonResponse<>(new LegacyMainResult(
                normalizedToken,
                legacyUser(user),
                authService.getLegacyAppInfo(normalizedToken),
                authService.getLegacySubMenus(normalizedToken, "")));
    }

    @ApiOperation("得到旧版应用信息")
    @PostMapping("/getapp")
    @ResponseBody
    public CommonResponse<LegacyAppResult> getApp(@RequestBody CommonRequest request) {
        return new CommonResponse<>(new LegacyAppResult(
                request.getToken(),
                authService.getLegacyAppInfo(request.getToken())));
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    @ResponseBody
    public CommonResponse<Void> logout(@RequestBody CommonRequest request) {
        authService.logout(request.getToken());
        return new CommonResponse<>((Void) null);
    }

    @ApiOperation("得到旧版用户信息")
    @PostMapping("/getuserinfo")
    @ResponseBody
    public CommonResponse<LegacyUserInfoResult> getUserInfo(@RequestBody CommonRequest request) {
        UserDTO user = authService.getInfoByToken(request.getToken());
        return new CommonResponse<>(new LegacyUserInfoResult(request.getToken(), legacyUser(user)));
    }

    private static LegacyUserInfo legacyUser(UserDTO user) {
        LegacyUserInfo legacyUser = new LegacyUserInfo();
        legacyUser.setLoginName(user.getId());
        legacyUser.setUserName(user.getName());
        legacyUser.setUserId(longOrZero(user.getId()));
        return legacyUser;
    }

    private static String legacyRawToken(String token) {
        String value = token == null ? "" : token.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static long longOrZero(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Data
    public static class LegacyUserInfoResult {
        private final String token;
        private final LegacyUserInfo user;
    }

    @Data
    public static class LegacyUserInfo {
        private String loginName;
        private String userName;
        private long userId;
        private String companyName = "";
        private String departmentName = "";
        private String userAvtarUrl = "";
    }

    @Data
    public static class LegacyLoginRequest {
        @JsonAlias("UserId")
        private String userId;
        @JsonAlias("PassWord")
        private String passWord;
        @JsonAlias("DbId")
        private String dbId;
        @JsonAlias("CheckCode")
        private String checkCode;
        @JsonAlias("AppId")
        private String appId;
        @JsonAlias("AppKey")
        private String appKey;
        @JsonAlias("CheckCodeKey")
        private String checkCodeKey;
    }

    @Data
    public static class LegacyLoginResult {
        private final String token;
        private final boolean loginSucess;
        private final LegacyUserInfo user;
        private final AuthService.LegacyAppInfo app;
        private final LegacyError error;

        static LegacyLoginResult success(String token, LegacyUserInfo user, AuthService.LegacyAppInfo app) {
            return new LegacyLoginResult(token, true, user, app, null);
        }

        static LegacyLoginResult error(int code, String message) {
            return new LegacyLoginResult(null, false, null, null, new LegacyError(code, message));
        }
    }

    @Data
    public static class LegacyError {
        private final int code;
        private final String message;
    }

    @Data
    public static class LegacySubMenuRequest extends CommonRequest {
        @JsonAlias("ParentAuthCode")
        private String parentAuthCode;
    }

    @Data
    public static class LegacySubMenuResult {
        private final String token;
        private final List<AuthService.LegacyAuthItem> items;
    }

    @Data
    public static class LegacyMainResult {
        private final String token;
        private final LegacyUserInfo user;
        private final AuthService.LegacyAppInfo app;
        private final List<AuthService.LegacyAuthItem> topMenu;
    }

    @Data
    public static class LegacyAppResult {
        private final String token;
        private final AuthService.LegacyAppInfo app;
    }
}
