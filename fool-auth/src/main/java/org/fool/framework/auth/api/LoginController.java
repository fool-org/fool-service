package org.fool.framework.auth.api;


import org.fool.framework.auth.business.model.Auth;
import org.fool.framework.auth.business.service.AuthService;
import org.fool.framework.auth.dto.LoginDTO;
import org.fool.framework.auth.dto.LoginVo;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.common.data.tree.TreeNode;
import org.fool.framework.dto.CommonRequest;
import org.fool.framework.dto.CommonResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/auth")
@RestController
@Api("用户登录相关接口")
public class LoginController {


    @Autowired
    private AuthService authService;

    @ApiOperation("登录")
    @PostMapping("/login")
    @ResponseBody
    public CommonResponse login(@RequestBody LoginDTO loginDTO) {
        var user = authService.login(loginDTO.getUserId(), loginDTO.getPassword());
        return new CommonResponse<LoginVo>(user);

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
}
