package org.xian.token.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.xian.token.MyResponse;
import org.xian.token.entity.SysUser;
import org.xian.token.mapper.SysUserMapper;
import org.xian.token.secutiry.TokenUtils;

import javax.annotation.Resource;

/**
 * 实现用户服务的接口
 *
 * @author xian
 */

@Service
public class SysUserService {
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    @Qualifier("authUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Resource
    private TokenUtils tokenUtils;

    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 用户登录
     *
     * @param sysUser 用户登录信息
     * @return 用户登录成功返回的Token
     */
    public MyResponse login(final SysUser sysUser) {
        String status;
        // 验证用户名和密码是否对的
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(sysUser.getUsername(),
                            sysUser.getPassword()));
            status = "SUCCESS";
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("用户名或密码不正确");
        }
        // 生成Token与查询用户权限
        final var userDetails = userDetailsService.loadUserByUsername(sysUser.getUsername());
        return new MyResponse(status,
                tokenUtils.createToken(userDetails));
    }

    /**
     * 用户注册
     *
     * @param sysUser 用户注册信息
     * @return 用户注册结果
     */
    public MyResponse save(SysUser sysUser) throws DataAccessException {
        try {
            sysUserMapper.insert(sysUser);
        } catch (DataAccessException e) {
            return new MyResponse("ERROR", "已经存在该用户名或者用户昵称，或者用户权限出错");
        }
        return new MyResponse("SUCCESS", "用户新增成功");
    }


}
