package org.xian.shiro.auth;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Service;
import org.xian.shiro.entity.SysUser;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author xian
 */
@Service
public class ShiroRealm extends AuthorizingRealm {
    @Resource
    TokenUtils tokenUtils;

    @Override
    public boolean supports(AuthenticationToken authenticationToken) {
        // 指定当前 authenticationToken 需要为 ShiroAuthToken 的实例
        return authenticationToken instanceof ShiroAuthToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        ShiroAuthToken shiroAuthToken = (ShiroAuthToken) authenticationToken;
        String token = (String) shiroAuthToken.getCredentials();
        // 验证 Token
        SysUser sysUser = tokenUtils.validationToken(token);
        if (sysUser == null || sysUser.getUsername() == null || sysUser.getUserRole() == null) {
            throw new AuthenticationException("Token 无效");
        }
        return new SimpleAuthenticationInfo(token,
                token, "ShiroRealm");
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        // 获取用户信息
        SysUser sysUser = tokenUtils.validationToken(principals.toString());
        // 创建一个授权对象
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 判断用户角色是否存在
        if (!sysUser.getUserRole().isEmpty()) {
            // 角色设置
            info.addRole(sysUser.getUserRole());
        }
        if (!sysUser.getUserPermission().isEmpty()) {
            // 进行权限设置,根据 , 分割
            Arrays.stream(sysUser.getUserPermission().split(",")).forEach(info::addStringPermission);
        }
        return info;
    }
}

