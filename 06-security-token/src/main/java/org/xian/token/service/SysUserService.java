package org.xian.token.service;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
        try {
            // 验证用户名和密码是否对的
            System.out.println(sysUser.getUsername());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(sysUser.getUsername(),
                            sysUser.getPassword()));
        } catch (BadCredentialsException e) {
            return new MyResponse("ERROR", "用户名或者密码不正确");
        }
        // 生成Token与查询用户权限
        SysUser sysUserData = sysUserMapper.selectByUsername(sysUser.getUsername());
        return new MyResponse("SUCCESS",
                tokenUtils.createToken(sysUserData));
    }

    /**
     * 用户注册
     *
     * @param sysUser 用户注册信息
     * @return 用户注册结果
     */
    public MyResponse save(SysUser sysUser) throws DataAccessException {
        try {
            // 密码加密存储
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String password = bCryptPasswordEncoder.encode(sysUser.getPassword());
            sysUser.setPassword(password);
            sysUserMapper.insert(sysUser);
        } catch (DataAccessException e) {
            return new MyResponse("ERROR", "已经存在该用户名或者用户昵称，或者用户权限出错");
        }
        return new MyResponse("SUCCESS", "用户新增成功");
    }
}
