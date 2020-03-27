package org.xian.security.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xian.security.entity.SysUser;
import org.xian.security.mapper.SysUserMapper;

import javax.annotation.Resource;

/**
 * @author xian
 */
@Controller
public class RegisterController {

    @Resource
    private SysUserMapper sysUserMapper;

    @RequestMapping("/register")
    public String register() {
        return "register";
    }

    @RequestMapping("/register-error")
    public String registerError(Model model) {
        model.addAttribute("error", true);
        return "register";
    }

    @RequestMapping("/register-save")
    public String registerSave(@ModelAttribute SysUser sysUser,
                               Model model) {
        // 判断 username password 不能为空
        if (sysUser.getUsername() == null || sysUser.getPassword() == null || sysUser.getUserRole() == null) {
            model.addAttribute("error", true);
            return "register";
        }
        try {
            // 密码加密存储
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            String password = bCryptPasswordEncoder.encode(sysUser.getPassword());
            sysUser.setPassword(password);
            // 写入数据库
            sysUserMapper.insert(sysUser);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", true);
            return "register";
        }
    }
}
