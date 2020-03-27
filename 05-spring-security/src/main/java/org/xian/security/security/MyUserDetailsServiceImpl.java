package org.xian.security.config.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.xian.security.entity.SysUser;
import org.xian.security.mapper.SysUserMapper;
import org.xian.security.service.SysUserService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xian
 */
@Service
public class MyUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (null == sysUser) {
            throw new UsernameNotFoundException(username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + sysUser.getUserRole()));
        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
