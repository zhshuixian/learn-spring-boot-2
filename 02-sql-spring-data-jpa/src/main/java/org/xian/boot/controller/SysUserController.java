package org.xian.boot.controller;

import org.hibernate.validator.constraints.Range;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.xian.boot.MyResponse;
import org.xian.boot.entity.SysUser;
import org.xian.boot.service.SysUserService;

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

    @PostMapping(value = "/save")
    public MyResponse save(@RequestBody SysUser sysUser) {
        return sysUserService.save(sysUser);
    }

    @PostMapping(value = "/find")
    public SysUser find(@RequestBody String username) {
        return sysUserService.find(username);
    }

    @PostMapping(value = "/update")
    public MyResponse update(@RequestBody SysUser sysUser) {
        return sysUserService.update(sysUser);
    }

    @PostMapping(value = "/delete")
    public MyResponse delete(@RequestBody String username) {
        return sysUserService.delete(username);
    }

    @PostMapping(value = "/saveAll")
    public MyResponse saveAll(@RequestBody List<SysUser> sysUserList) {
        return sysUserService.saveAll(sysUserList);
    }

    @GetMapping(value = "list")
    public List<SysUser> list() {
        return sysUserService.list();
    }

    @PostMapping(value = "page")
    public Page<SysUser> page(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "3") Integer size) {
        return sysUserService.page(page, size);
    }

    @PostMapping(value = "search")
    public Page<SysUser> search(@RequestParam String nickname, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "3") Integer size) {
        return sysUserService.searchByNickname(nickname, page, size);
    }

}
