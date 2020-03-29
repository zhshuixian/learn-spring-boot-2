package org.xian.token.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xian.token.MyResponse;
import org.xian.token.entity.SysUser;
import org.xian.token.service.SysUserService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 实现用户功能接口API
 *
 * @author xian
 */
@RestController
@RequestMapping(value = "/api/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    /**
     * 用户登录接口
     *
     * @param sysUser 用户登录的用户名和密码
     * @return 用户Token和角色
     * @throws AuthenticationException 身份验证错误抛出异常
     */
    @PostMapping(value = "/login")
    public MyResponse login(@RequestBody final SysUser sysUser) throws AuthenticationException {
        return sysUserService.login(sysUser);
    }

    /**
     * 用户注册接口 需要管理元权限
     *
     * @param sysUser 用户注册信息
     * @return 用户注册结果
     */
    @PostMapping(value = "/register")
    @PreAuthorize("hasRole('ADMIN')")
    public MyResponse register(@RequestBody @Valid final SysUser sysUser) {
        return sysUserService.save(sysUser);
    }
}
