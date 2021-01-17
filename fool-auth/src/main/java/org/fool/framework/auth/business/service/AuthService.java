package org.fool.framework.auth.business.service;

import org.fool.framework.auth.business.common.BusinessErrorCode;
import org.fool.framework.auth.business.model.Auth;
import org.fool.framework.auth.business.model.User;
import org.fool.framework.auth.dto.LoginVo;
import org.fool.framework.auth.dto.UserDTO;
import org.fool.framework.common.data.tree.ITreeFactory;
import org.fool.framework.common.data.tree.TreeNode;
import org.fool.framework.common.data.tree.TreeNodeCompareResult;
import org.fool.framework.dao.DaoService;
import org.fool.framework.dto.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@Slf4j
public class AuthService {


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
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("md5");
                md5.update((user.getId().toString() + password).getBytes());
                String info = new String(md5.digest());
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

    public User register(String id, String password, String name, String mobile) {
        try {
            var md5 = MessageDigest.getInstance("md5");
            User user = new User();
            user.setId(id);
            user.setName(name);
            user.setMobile(mobile);
            user.setPassword(new String(md5.digest((id + password).getBytes())));
            daoService.create(user);
            return user;
        } catch (Exception ex) {
            log.info("", ex);

        }
        return null;
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
}
