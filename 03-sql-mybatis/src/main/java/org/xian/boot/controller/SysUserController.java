package org.xian.boot.controller;


import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import org.xian.boot.MyResponse;
import org.xian.boot.service.SysUserService;
import org.xian.boot.entity.SysUser;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author xian
 */
@RestController
@RequestMapping(value = "/api/user")
public class SysUserController {
    @Resource
    private SysUserService sysUserService;

    @PostMapping(value = "/insert")
    public MyResponse insert(@RequestBody SysUser sysUser) {
        return sysUserService.insert(sysUser);
    }

    @PostMapping(value = "select")
    public SysUser select(@RequestBody Long userId) {
        return sysUserService.select(userId);
    }

    @PostMapping(value = "/update")
    public MyResponse update(@RequestBody SysUser sysUser) {
        return sysUserService.update(sysUser);
    }

    @PostMapping(value = "delete")
    public MyResponse delete(@RequestBody Long userId) {
        return sysUserService.delete(userId);
    }

    @GetMapping("selectAll")
    public List<SysUser> selectAll() {
        return sysUserService.selectAll();
    }

    @GetMapping("selectPage")
    public PageInfo<SysUser> selectPage(@RequestParam(defaultValue = "0") Integer page,
                                        @RequestParam(defaultValue = "3") Integer size) {
        return sysUserService.selectPage(page, size);
    }
}
